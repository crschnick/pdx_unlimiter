package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SavegameHelper {

    public static class SavegameContext<T, I extends SavegameInfo<T>> {

        private SavegameInfo<T> info;
        private SavegameEntry<T,I> entry;
        private SavegameCollection<T,I> collection;
        private GameIntegration<T,I> integration;

        public SavegameInfo<T> getInfo() {
            return info;
        }

        public SavegameEntry<T, I> getEntry() {
            return entry;
        }

        public SavegameCollection<T, I> getCollection() {
            return collection;
        }

        public GameIntegration<T,I> getIntegration() {
            return integration;
        }
    }

    private static <T, I extends SavegameInfo<T>> SavegameStorage<T, I> getForSavegame(SavegameEntry<T, I> e) {
        @SuppressWarnings("unchecked")
        Optional<SavegameStorage<T, I>> sg = SavegameStorage.ALL.stream()
                .filter(i -> i.contains(e))
                .findFirst()
                .map(v -> (SavegameStorage<T, I>) v);
        return sg.orElseThrow(IllegalArgumentException::new);
    }

    private static <T, I extends SavegameInfo<T>> SavegameStorage<T, I> getForCollection(SavegameCollection<T, I> col) {
        @SuppressWarnings("unchecked")
        Optional<SavegameStorage<T, I>> sg = SavegameStorage.ALL.stream()
                .filter(i -> i.getCollections().contains(col))
                .findFirst()
                .map(v -> (SavegameStorage<T, I>) v);
        return sg.orElseThrow(IllegalArgumentException::new);
    }

    public static <T, I extends SavegameInfo<T>> void withCollection(
            SavegameCollection<T, I> col,
            Consumer<GameIntegration<T, I>> con) {
        var st = getForCollection(col);
        if (st == null) {
            throw new IllegalStateException();
        }

        var gi = GameIntegration.getForSavegameStorage(st);
        if (gi == null) {
            throw new IllegalStateException();
        }

        con.accept(gi);
    }

    public static <T, I extends SavegameInfo<T>> void withSavegameAsync(
            SavegameEntry<T, I> e,
            Consumer<SavegameContext<T,I>> con) {
        if (e.getInfo() != null) {
            withSavegame(e, con::accept);
        } else {
            e.infoProperty().addListener(new javafx.beans.value.ChangeListener<I>() {
                @Override
                public void changed(ObservableValue<? extends I> observable, I oldValue, I newValue) {
                    if (newValue != null) {
                        Platform.runLater(() -> {
                            withSavegame(e, con::accept);
                        });
                    } else {
                        // Remove listener if info is unloaded
                        e.infoProperty().removeListener(this);
                    }
                }
            });
        }
    }

    public static <T, I extends SavegameInfo<T>> void withSavegame(
            SavegameEntry<T, I> e, Consumer<SavegameContext<T, I>> con) {
        mapSavegame(e, (ctx) -> {
            con.accept(ctx);
            return null;
        });
    }

    public static <T, I extends SavegameInfo<T>, R> R mapSavegame(
            SavegameEntry<T, I> e, Function<SavegameContext<T, I>, R> con) {
        var st = getForSavegame(e);
        if (st == null) {
            throw new IllegalStateException();
        }

        var gi = GameIntegration.getForSavegameStorage(st);
        if (gi == null) {
            throw new IllegalStateException();
        }

        var col = gi.getSavegameStorage().getSavegameCollection(e);
        if (col == null) {
            throw new IllegalStateException();
        }

        var ctx = new SavegameContext<T,I>();
        ctx.info = e.getInfo();
        ctx.entry = e;
        ctx.collection = col;
        ctx.integration = gi;

        return con.apply(ctx);
    }
}
