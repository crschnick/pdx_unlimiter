package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.gui.game.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SavegameHelper {

    public static <T, I extends SavegameInfo<T>> Game getForSavegame(SavegameEntry<T, I> e) {
        Optional<Game> sg = SavegameStorage.ALL.entrySet().stream()
                .filter(kv -> kv.getValue().contains(e))
                .findFirst()
                .map(v -> v.getKey());
        return sg.orElseThrow(IllegalArgumentException::new);
    }

    public static <T, I extends SavegameInfo<T>> Game getForCollection(SavegameCollection<T, I> col) {
        Optional<Game> sg = SavegameStorage.ALL.entrySet().stream()
                .filter(kv -> kv.getValue().getCollections().contains(col))
                .findFirst()
                .map(v -> v.getKey());
        return sg.orElseThrow(IllegalArgumentException::new);
    }

    public static <T, I extends SavegameInfo<T>> void withCollection(
            SavegameCollection<T, I> col,
            Consumer<SavegameContext<T, I>> con) {
        var g = getForCollection(col);
        if (g == null) {
            throw new IllegalStateException();
        }

        var ctx = new SavegameContext<T, I>();
        ctx.storage = SavegameStorage.get(g);
        ctx.installation = GameInstallation.ALL.get(g);
        ctx.game = g;
        ctx.guiFactory = GameGuiFactory.get(g);
        ctx.info = null;
        ctx.entry = null;
        ctx.collection = col;
        con.accept(ctx);
    }

    public static <T, I extends SavegameInfo<T>> void withSavegameAsync(
            SavegameEntry<T, I> e,
            Consumer<SavegameContext<T, I>> con) {
        if (e.getInfo() != null) {
            withSavegame(e, con);
        } else {
            e.infoProperty().addListener(new javafx.beans.value.ChangeListener<I>() {
                @Override
                public void changed(ObservableValue<? extends I> observable, I oldValue, I newValue) {
                    if (newValue != null) {
                        Platform.runLater(() -> {
                            withSavegame(e, con);
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
        var g = getForSavegame(e);
        if (g == null) {
            throw new IllegalStateException();
        }

        var ctx = new SavegameContext<T, I>();
        ctx.storage = SavegameStorage.get(g);
        ctx.installation = GameInstallation.ALL.get(g);
        ctx.guiFactory = GameGuiFactory.get(g);
        ctx.game = g;
        ctx.info = e.getInfo();
        ctx.entry = e;

        var col = SavegameStorage.<T, I>get(g).getSavegameCollection(e);
        if (col == null) {
            throw new IllegalStateException();
        }
        ctx.collection = col;

        return con.apply(ctx);
    }

    public static class SavegameContext<T, I extends SavegameInfo<T>> {

        private Game game;
        private GameInstallation installation;
        private GameGuiFactory<T, I> guiFactory;
        private SavegameStorage<T, I> storage;
        private SavegameInfo<T> info;
        private SavegameEntry<T, I> entry;
        private SavegameCollection<T, I> collection;

        public Game getGame() {
            return game;
        }

        public SavegameInfo<T> getInfo() {
            return info;
        }

        public SavegameEntry<T, I> getEntry() {
            return entry;
        }

        public SavegameCollection<T, I> getCollection() {
            return collection;
        }

        public GameInstallation getInstallation() {
            return installation;
        }

        public SavegameStorage<T, I> getStorage() {
            return storage;
        }

        public GameGuiFactory<T, I> getGuiFactory() {
            return guiFactory;
        }
    }
}
