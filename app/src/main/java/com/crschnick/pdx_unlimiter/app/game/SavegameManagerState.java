package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.SavedState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SavegameManagerState {

    private static SavegameManagerState INSTANCE = new SavegameManagerState();

    public static SavegameManagerState get() {
        return INSTANCE;
    }

    public static void init() {
        SavedState s = SavedState.getInstance();
        for (var gi : GameIntegration.ALL) {
            if (s.getActiveGame().equals(gi.getInstallation())) {
                INSTANCE.current.set(gi);
                return;
            }
        }

        if (GameIntegration.ALL.size() > 0 && INSTANCE.current.isNull().get()) {
            INSTANCE.current.set(GameIntegration.ALL.get(0));
        }
    }

    public static void reset() {
        if (INSTANCE.current() != null) {
            INSTANCE.selectIntegration(null);
        }
    }

    private SimpleObjectProperty<GameIntegration<?, ? extends SavegameInfo<?>>> current = new SimpleObjectProperty<>();

    private SimpleObjectProperty<? extends GameCampaign<?, ? extends SavegameInfo<?>>> globalSelectedCampaign =
            new SimpleObjectProperty<>();

    private SimpleObjectProperty<? extends GameCampaignEntry<?, ? extends SavegameInfo<?>>> globalSelectedEntry =
            new SimpleObjectProperty<>();

    public <T, I extends SavegameInfo<T>> ReadOnlyObjectProperty<GameCampaign<T, I>> globalSelectedCampaignProperty() {
        return (SimpleObjectProperty<GameCampaign<T, I>>) globalSelectedCampaign;
    }

    private <T, I extends SavegameInfo<T>> SimpleObjectProperty<GameCampaign<T, I>> globalSelectedCampaignPropertyInternal() {
        return (SimpleObjectProperty<GameCampaign<T, I>>) globalSelectedCampaign;
    }

    public <T, I extends SavegameInfo<T>>
    ReadOnlyObjectProperty<GameCampaignEntry<T, I>> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<GameCampaignEntry<T, I>>) globalSelectedEntry;
    }

    private <T, I extends SavegameInfo<T>>
    SimpleObjectProperty<GameCampaignEntry<T, I>> globalSelectedEntryPropertyInternal() {
        return (SimpleObjectProperty<GameCampaignEntry<T, I>>) globalSelectedEntry;
    }

    public <T, I extends SavegameInfo<T>> GameIntegration<T, I> current() {
        return (GameIntegration<T, I>) current.get();
    }

    public <T, I extends SavegameInfo<T>, G extends GameIntegration<T, I>> SimpleObjectProperty<G> currentGameProperty() {
        return (SimpleObjectProperty<G>) current;
    }

    private void unselectCampaignAndEntry() {
        if (current.isNotNull().get()) {
            globalSelectedEntryPropertyInternal().set(null);
            globalSelectedCampaignPropertyInternal().set(null);
        }
    }

    public boolean selectIntegration(GameIntegration<?, ?> newInt) {
        if (current.get() == newInt) {
            return false;
        }

        unselectCampaignAndEntry();

        current.set(newInt);
        LoggerFactory.getLogger(GameIntegration.class).debug("Selected integration " + (newInt != null ? newInt.getName() : "null"));
        return true;
    }

    public <T, I extends SavegameInfo<T>> void selectCampaign(GameCampaign<T, I> c) {
        if (c == null) {
            unselectCampaignAndEntry();
            LoggerFactory.getLogger(GameIntegration.class).debug("Unselected campaign");
            return;
        }

        if (globalSelectedCampaign.isNotNull().get() && globalSelectedCampaign.get().equals(c)) {
            return;
        }

        Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameCache().getCampaigns().contains(c))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        gi.ifPresentOrElse(v -> {
            // If we didn't change the game and an entry is already selected, unselect it
            if (!selectIntegration(v) && globalSelectedEntryProperty().isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }

            globalSelectedCampaignPropertyInternal().set((GameCampaign<Object, SavegameInfo<Object>>) c);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign " + c.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign " + c.getName());
            selectIntegration(null);
        });
    }

    public <T, I extends SavegameInfo<T>> void selectEntry(GameCampaignEntry<T, I> e) {
        if (globalSelectedEntryPropertyInternal().isEqualTo(e).get()) {
            return;
        }

        if (e == null) {
            if (current.isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }
            return;
        }

        Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameCache().contains(e))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        gi.ifPresentOrElse(v -> {
            selectCampaign(v.getSavegameCache().getCampaign(e));

            globalSelectedEntryPropertyInternal().set((GameCampaignEntry<Object, SavegameInfo<Object>>) e);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign entry " + e.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign entry " + e.getName());
        });
    }
}
