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

    public static <T, I extends SavegameInfo<T>> void withSavegame(
            SavegameEntry<T, I> e,
            BiConsumer<SavegameInfo<T>, GameIntegration<T, I>> con) {
        mapSavegame(e, (info, gi) -> {
            con.accept(info, gi);
            return null;
        });
    }

    public static <T, I extends SavegameInfo<T>, R> Optional<R> mapSavegame(
            SavegameEntry<T, I> e,
            BiFunction<SavegameInfo<T>, GameIntegration<T, I>, R> con) {
        if (e.getInfo() != null) {
            return Optional.ofNullable(mapSavegame(e, gi -> con.apply(e.getInfo(), gi)));
        }
        return Optional.empty();
    }

    public static <T, I extends SavegameInfo<T>> void withSavegameAsync(
            SavegameEntry<T, I> e,
            BiConsumer<SavegameInfo<T>, GameIntegration<T, I>> con) {
        if (e.getInfo() != null) {
            withSavegame(e, gi -> con.accept(e.getInfo(), gi));
        } else {
            // Remove listener if info is unloaded
            e.infoProperty().addListener(new javafx.beans.value.ChangeListener<I>() {
                @Override
                public void changed(ObservableValue<? extends I> observable, I oldValue, I newValue) {
                    if (newValue != null) {
                        Platform.runLater(() -> {
                            withSavegame(e, gi -> con.accept(newValue, gi));
                        });
                    } else {
                        e.infoProperty().removeListener(this);
                    }
                }
            });
        }
    }

    public static <T, I extends SavegameInfo<T>> void withSavegame(
            SavegameEntry<T, I> e, Consumer<GameIntegration<T, I>> con) {
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
            return;
        }

        con.accept(gi);
    }

    public static <T, I extends SavegameInfo<T>, R> R mapSavegame(
            SavegameEntry<T, I> e, Function<GameIntegration<T, I>, R> con) {
        var st = getForSavegame(e);
        if (st == null) {
            throw new IllegalStateException();
        }

        var gi = GameIntegration.getForSavegameStorage(st);
        if (gi == null) {
            throw new IllegalStateException();
        }

        return con.apply(gi);
    }
}
