package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.install.GameIntegration;
import com.crschnick.pdx_unlimiter.app.core.SavedState;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;

public class SavegameManagerState<T, I extends SavegameInfo<T>> {

    private static SavegameManagerState<?, ?> INSTANCE = new SavegameManagerState<Object, SavegameInfo<Object>>();
    private SimpleObjectProperty<GameIntegration<?, ? extends SavegameInfo<?>>> current = new SimpleObjectProperty<>();
    private SimpleObjectProperty<SavegameCollection<T, I>> globalSelectedCampaign =
            new SimpleObjectProperty<>();
    private SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntry =
            new SimpleObjectProperty<>();
    private Filter filter = new Filter();
    private ObservableList<SavegameCollection<T, I>> shownCollections = FXCollections.observableArrayList();
    private ObservableList<SavegameEntry<T, I>> shownEntries = FXCollections.observableArrayList();

    private SavegameManagerState() {
        var cl = (SetChangeListener<? super SavegameEntry<T, I>>) ch -> {
            updateShownEntries();
        };

        globalSelectedCampaign.addListener((c, o, n) -> {
            if (o != null) {
                o.getSavegames().removeListener(cl);
                SavegameStorage.getForSavegame(o).unloadCollectionAsync(o);
            }

            if (n != null) {
                n.getSavegames().addListener(cl);
            }
        });
    }

    public static <T, I extends SavegameInfo<T>> SavegameManagerState<T, I> get() {
        return (SavegameManagerState<T, I>) INSTANCE;
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

    public ReadOnlyObjectProperty<SavegameCollection<T, I>> globalSelectedCampaignProperty() {
        return globalSelectedCampaign;
    }

    private SimpleObjectProperty<SavegameCollection<T, I>> globalSelectedCampaignPropertyInternal() {
        return globalSelectedCampaign;
    }

    public ReadOnlyObjectProperty<SavegameEntry<T, I>> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<SavegameEntry<T, I>>) globalSelectedEntry;
    }

    private SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntryPropertyInternal() {
        return (SimpleObjectProperty<SavegameEntry<T, I>>) globalSelectedEntry;
    }

    public Filter getFilter() {
        return filter;
    }

    private void updateShownEntries() {
        // No integration or campaign selected means no entries are shown
        if (current() == null || globalSelectedCampaign.get() == null) {
            shownEntries.clear();
            return;
        }

        // Remove not contained entries
        shownEntries.removeIf(entry -> !globalSelectedCampaign.get().getSavegames().contains(entry));

        var col = globalSelectedCampaign.get();
        col.getSavegames().forEach(entry -> {
            if (!shownEntries.contains(entry) && filter.shouldShow(entry)) {
                shownEntries.add(entry);
                return;
            }

            if (shownEntries.contains(entry) && !filter.shouldShow(entry)) {
                shownEntries.remove(entry);
            }
        });
        shownEntries.sort(Comparator.comparing(SavegameEntry::getDate, Comparator.reverseOrder()));
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
        shownCollections.sort(Comparator.comparing(SavegameCollection::getLastPlayed, Comparator.reverseOrder()));
    }

    public ObservableList<SavegameEntry<T, I>> getShownEntries() {
        return shownEntries;
    }

    public ObservableList<SavegameCollection<T, I>> getShownCollections() {
        return shownCollections;
    }

    public GameIntegration<T, I> current() {
        return (GameIntegration<T, I>) current.get();
    }

    public <G extends GameIntegration<T, I>> SimpleObjectProperty<G> currentGameProperty() {
        return (SimpleObjectProperty<G>) current;
    }

    private void unselectCampaignAndEntry() {
        if (current.isNotNull().get()) {
            globalSelectedEntryPropertyInternal().set(null);
            globalSelectedCampaignPropertyInternal().set(null);
        }
        updateShownEntries();
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
                    (SetChangeListener<? super SavegameCollection<?, ?>>) c -> {
                        updateShownCollections();
                    });
        }
        return true;
    }

    public void selectCollection(SavegameCollection<T, I> c) {
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

            globalSelectedCampaignPropertyInternal().set(c);
            updateShownEntries();
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign " + c.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign " + c.getName());
            selectIntegration(null);
        });
    }

    public void selectEntry(SavegameEntry<T, I> e) {
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

            globalSelectedEntryPropertyInternal().set(e);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign entry " + e.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign entry " + e.getName());
        });
    }

    public class Filter {
        private StringProperty filter = new SimpleStringProperty("");

        private Filter() {
            filter.addListener((c, o, n) -> {
                SavegameManagerState.this.updateShownEntries();
                SavegameManagerState.this.updateShownCollections();
            });
        }

        public String getFilter() {
            return filter.get();
        }

        public StringProperty filterProperty() {
            return filter;
        }

        public boolean shouldShow(SavegameCollection<?, ?> col) {
            if (col.getName().toLowerCase().contains(filter.get().toLowerCase())) {
                return true;
            }

            return col.getSavegames().stream()
                    .anyMatch(e -> e.getName().toLowerCase().contains(filter.get().toLowerCase()));
        }

        public boolean shouldShow(SavegameEntry<?, ?> entry) {
            if (entry.getName().toLowerCase().contains(filter.get().toLowerCase())) {
                return true;
            }

            return false;
        }
    }
}
