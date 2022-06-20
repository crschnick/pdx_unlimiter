package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import javafx.beans.value.ObservableValue;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class SavegameContext<T, I extends SavegameInfo<T>> {

    private Game game;
    private GameInstallation installation;
    private GameGuiFactory<T, I> guiFactory;
    private SavegameStorage<T, I> storage;
    private SavegameInfo<T> info;
    private SavegameEntry<T, I> entry;
    private SavegameCampaign<T, I> collection;

    public static <T, I extends SavegameInfo<T>> Game getForSavegame(SavegameEntry<T, I> e) {
        Optional<Game> sg = SavegameStorage.ALL.entrySet().stream()
                .filter(kv -> kv.getValue().contains(e))
                .findFirst()
                .map(v -> v.getKey());
        return sg.orElse(null);
    }

    public static <T, I extends SavegameInfo<T>> Game getForCollection(SavegameCampaign<T, I> col) {
        Optional<Game> sg = SavegameStorage.ALL.entrySet().stream()
                .filter(kv -> kv.getValue().getCollections().contains(col))
                .findFirst()
                .map(v -> v.getKey());
        return sg.orElse(null);
    }

    public static <T, I extends SavegameInfo<T>> void withCollectionContext(
            SavegameCampaign<T, I> col,
            Consumer<SavegameContext<T, I>> con) {
        var g = getForCollection(col);
        if (g == null) {
            return;
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

    public static <T, I extends SavegameInfo<T>> void withSavegameInfoContextAsync(
            SavegameEntry<T, I> e,
            Consumer<SavegameContext<T, I>> con) {
        var ctx = getContextIfExistent(e);
        if (ctx.isEmpty()) {
            return;
        }

        if (ctx.get().getInfo() != null) {
            con.accept(ctx.get());
        } else {
            e.infoProperty().addListener(new javafx.beans.value.ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends I> observable, I oldValue, I newValue) {
                    if (newValue != null) {
                        var ctx = getContextIfExistent(e);
                        if (ctx.isEmpty()) {
                            return;
                        }

                        ctx.get().info = newValue;
                        con.accept(ctx.get());
                        e.infoProperty().removeListener(this);
                    } else {
                        // Remove listener if info is unloaded
                        e.infoProperty().removeListener(this);
                    }
                }
            });
        }
    }

    public static <T, I extends SavegameInfo<T>> void withSavegameContext(
            SavegameEntry<T, I> e, Consumer<SavegameContext<T, I>> con) {
        getContextIfExistent(e).ifPresent(con);
    }

    public static <T, I extends SavegameInfo<T>> Optional<SavegameContext<T, I>> getContextIfExistent(
            SavegameEntry<T, I> e) {
        var g = getForSavegame(e);
        if (g == null) {
            return Optional.empty();
        }

        var ctx = new SavegameContext<T, I>();
        ctx.storage = SavegameStorage.get(g);
        ctx.installation = GameInstallation.ALL.get(g);
        ctx.guiFactory = GameGuiFactory.get(g);
        ctx.game = g;
        ctx.info = e.getInfo();
        ctx.entry = e;

        var col = SavegameStorage.<T, I>get(g).getSavegameCampaign(e);
        if (col == null) {
            return Optional.empty();
        }
        ctx.collection = col;
        return Optional.of(ctx);
    }

    public static <T, I extends SavegameInfo<T>> SavegameContext<T, I> getContext(
            SavegameEntry<T, I> e) {
        return getContextIfExistent(e).orElseThrow(
                () -> new IllegalStateException("Savegame is not stored (anymore)"));
    }

    public static <T, I extends SavegameInfo<T>, R> R mapSavegame(
            SavegameEntry<T, I> e, Function<SavegameContext<T, I>, R> con) {
        var ctx = getContext(e);
        return con.apply(ctx);
    }

    public Game getGame() {
        return game;
    }

    public SavegameInfo<T> getInfo() {
        return info;
    }

    public SavegameEntry<T, I> getEntry() {
        return entry;
    }

    public SavegameCampaign<T, I> getCollection() {
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
