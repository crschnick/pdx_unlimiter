package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.SavedState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameFolder;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.logging.Handler;
import java.util.stream.Collectors;

public class SavegameManagerState {

    public class Filter {
        private StringProperty filter = new SimpleStringProperty("");

        private Filter() {
            filter.addListener((c,o,n) -> {
                SavegameManagerState.this.updateShownCollections();
            });
        }

        public String getFilter() {
            return filter.get();
        }

        public StringProperty filterProperty() {
            return filter;
        }

        public boolean shouldShow(SavegameCollection<?,?> col) {
            if (col.getName().toLowerCase().contains(filter.get().toLowerCase())) {
                return true;
            }

            return col.getSavegames().stream()
                    .anyMatch(e -> e.getName().toLowerCase().contains(filter.get().toLowerCase()));
        }
    }

    private static SavegameManagerState INSTANCE = new SavegameManagerState();

    public static SavegameManagerState get() {
        return INSTANCE;
    }

    public static void init() {
        SavedState s = SavedState.getInstance();
        for (var gi : GameIntegration.ALL) {
            if (s.getActiveGame().equals(gi.getInstallation())) {
                INSTANCE.selectIntegration(gi);
                return;
            }
        }

        if (GameIntegration.ALL.size() > 0 && INSTANCE.current.isNull().get()) {
            INSTANCE.selectIntegration(GameIntegration.ALL.get(0));
        }
    }

    public static void reset() {
        if (INSTANCE.current() != null) {
            INSTANCE.selectIntegration(null);
        }
    }

    private SimpleObjectProperty<GameIntegration<?, ? extends SavegameInfo<?>>> current = new SimpleObjectProperty<>();

    private SimpleObjectProperty<? extends SavegameCollection<?, ? extends SavegameInfo<?>>> globalSelectedCampaign =
            new SimpleObjectProperty<>();

    private SimpleObjectProperty<? extends GameCampaignEntry<?, ? extends SavegameInfo<?>>> globalSelectedEntry =
            new SimpleObjectProperty<>();

    public <T, I extends SavegameInfo<T>> ReadOnlyObjectProperty<SavegameCollection<T, I>> globalSelectedCampaignProperty() {
        return (SimpleObjectProperty<SavegameCollection<T, I>>) globalSelectedCampaign;
    }

    private <T, I extends SavegameInfo<T>> SimpleObjectProperty<SavegameCollection<T, I>> globalSelectedCampaignPropertyInternal() {
        return (SimpleObjectProperty<SavegameCollection<T, I>>) globalSelectedCampaign;
    }

    public <T, I extends SavegameInfo<T>>
    ReadOnlyObjectProperty<GameCampaignEntry<T, I>> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<GameCampaignEntry<T, I>>) globalSelectedEntry;
    }

    private <T, I extends SavegameInfo<T>>
    SimpleObjectProperty<GameCampaignEntry<T, I>> globalSelectedEntryPropertyInternal() {
        return (SimpleObjectProperty<GameCampaignEntry<T, I>>) globalSelectedEntry;
    }

    private Filter filter = new Filter();
    private ObservableSet<SavegameCollection<?,?>> shownCollections = FXCollections.observableSet();

    public Filter getFilter() {
        return filter;
    }

    private void updateShownCollections() {
        if (current() == null) {
            shownCollections.clear();
            return;
        }

        shownCollections.removeIf(col -> !current().getSavegameCache().getCollections().contains(col));

        current().getSavegameCache().getCollections().forEach(col -> {
            if (!shownCollections.contains(col) && filter.shouldShow(col)) {
                shownCollections.add(col);
                return;
            }

            if (shownCollections.contains(col) && !filter.shouldShow(col)) {
                shownCollections.remove(col);
            }
        });
    }

    public int indexOf(SavegameCollection<?, ?> c) {
        var list = new ArrayList<>(shownCollections);
        list.sort(Comparator.comparing(SavegameCollection::getLastPlayed));
        Collections.reverse(list);
        return list.indexOf(c);
    }

    public ObservableSet<SavegameCollection<?, ?>> shownCollectionsProperty() {
        return shownCollections;
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
        updateShownCollections();
        if (newInt != null) {
            newInt.getSavegameCache().getCollections().addListener(
                    (SetChangeListener<? super SavegameCollection<?,?>>) c -> {
                updateShownCollections();
            });
        }
        return true;
    }

    public <T, I extends SavegameInfo<T>> void selectCollection(SavegameCollection<T, I> c) {
        if (c == null) {
            unselectCampaignAndEntry();
            LoggerFactory.getLogger(GameIntegration.class).debug("Unselected campaign");
            return;
        }

        if (globalSelectedCampaign.isNotNull().get() && globalSelectedCampaign.get().equals(c)) {
            return;
        }

        Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameCache().getCollections().contains(c))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        gi.ifPresentOrElse(v -> {
            // If we didn't change the game and an entry is already selected, unselect it
            if (!selectIntegration(v) && globalSelectedEntryProperty().isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }

            globalSelectedCampaignPropertyInternal().set((SavegameCollection<Object, SavegameInfo<Object>>) c);
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
            selectCollection(v.getSavegameCache().getSavegameCollection(e));

            globalSelectedEntryPropertyInternal().set((GameCampaignEntry<Object, SavegameInfo<Object>>) e);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign entry " + e.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign entry " + e.getName());
        });
    }
}
