package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;

import javax.swing.event.ChangeListener;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class SavegameInfoHelper {

    public static <T, I extends SavegameInfo<T>> void withInfo(
            SavegameEntry<T,I> e,
            BiConsumer<SavegameInfo<T>,GameIntegration<T,I>> con) {
        withInfo(e, (info, gi) -> {
            con.accept(info, gi);
            return null;
        });
    }

    public static <T, I extends SavegameInfo<T>, R> Optional<R> withInfo(
            SavegameEntry<T,I> e,
            BiFunction<SavegameInfo<T>,GameIntegration<T,I>,R> con) {
        if (e.getInfo() != null) {
            createWithIntegration(e, gi -> con.apply(e.getInfo(), gi));
        }
        return Optional.empty();
    }

    public static <T, I extends SavegameInfo<T>> void withInfoAsync(
            SavegameEntry<T,I> e,
            BiConsumer<SavegameInfo<T>,GameIntegration<T,I>> con) {
        if (e.getInfo() != null) {
            doWithIntegration(e, gi -> con.accept(e.getInfo(), gi));
        } else {
            // Remove listener if info is unloaded
            e.infoProperty().addListener(new javafx.beans.value.ChangeListener<I>() {
                @Override
                public void changed(ObservableValue<? extends I> observable, I oldValue, I newValue) {
                    if (newValue != null) {
                        Platform.runLater(() -> {
                            doWithIntegration(e, gi -> con.accept(newValue, gi));
                        });
                    } else {
                        e.infoProperty().removeListener(this);
                    }
                }
            });
        }
    }

    public static <T, I extends SavegameInfo<T>> void doWithIntegration(
            SavegameEntry<T,I> e, Consumer<GameIntegration<T,I>> con) {
        var st = SavegameStorage.getForSavegame(e);
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

    public static <T, I extends SavegameInfo<T>, R> R createWithIntegration(
            SavegameEntry<T,I> e, Function<GameIntegration<T,I>,R> con) {
        var st = SavegameStorage.getForSavegame(e);
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
