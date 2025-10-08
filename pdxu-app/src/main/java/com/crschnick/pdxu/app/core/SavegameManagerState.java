package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;

public class SavegameManagerState<T, I extends SavegameInfo<T>> {

    @Getter
    private final Game game;
    private final SimpleObjectProperty<SavegameCampaign<T, I>> globalSelectedCollection =
            new SimpleObjectProperty<>();
    private final SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntry =
            new SimpleObjectProperty<>();
    private final Filter filter = new Filter();
    private final ListProperty<SavegameCampaign<T, I>> shownCollections = new SimpleListProperty<>(
            FXCollections.observableList(new CopyOnWriteArrayList<>()));
    private final ListProperty<SavegameEntry<T, I>> shownEntries = new SimpleListProperty<>(
            FXCollections.observableList(new CopyOnWriteArrayList<>()));
    private final BooleanProperty storageEmpty = new SimpleBooleanProperty();

    public SavegameManagerState(Game game) {
        this.game = game;
        addShownContentChangeListeners();
        updateShownCollections();
    }

    private void addShownContentChangeListeners() {
        var colListener = (SetChangeListener<? super SavegameCampaign<Object, SavegameInfo<Object>>>) c -> {
            updateShownCollections();
            updateShownEntries();
        };
        SavegameStorage.get(game).getCollections().addListener(colListener);

        var cl = (SetChangeListener<? super SavegameEntry<T, I>>) ch -> updateShownEntries();
        globalSelectedCollection.addListener((c, o, n) -> {
            if (o != null) {
                o.getSavegames().removeListener(cl);
                unloadCollectionAsync(o);
            }

            if (n != null) {
                n.getSavegames().addListener(cl);
            }
        });
    }

    public ReadOnlyObjectProperty<SavegameCampaign<T, I>> globalSelectedCollectionProperty() {
        return globalSelectedCollection;
    }

    private SimpleObjectProperty<SavegameCampaign<T, I>> globalSelectedCampaignPropertyInternal() {
        return globalSelectedCollection;
    }

    public ReadOnlyObjectProperty<SavegameEntry<T, I>> globalSelectedEntryProperty() {
        return globalSelectedEntry;
    }

    private SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntryPropertyInternal() {
        return globalSelectedEntry;
    }

    public Filter getFilter() {
        return filter;
    }

    public void loadEntryAsync(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> SavegameContext.withSavegameContext(e, ctx -> {
            if (globalSelectedCampaignPropertyInternal().get() != null &&
                    globalSelectedCampaignPropertyInternal().get().equals(
                            ctx.getStorage().getSavegameCampaign(e))) {

                ctx.getStorage().loadEntry(e);
            }
        }), false);
    }

    public void unloadCollectionAsync(SavegameCampaign<T, I> col) {
        TaskExecutor.getInstance().submitTask(() -> {
            TrackEvent.debug("Unloading collection " + col.getName());
            for (var e : col.getSavegames()) {
                e.setInactive();
            }
        }, false);
    }

    private void updateShownEntries() {
        // No campaign selected means no entries are shown
        if (globalSelectedCollection.get() == null) {
            shownEntries.clear();
            return;
        }

        // Work on copy to reduce list updates
        var newEntries = FXCollections.observableArrayList(shownEntries.get());

        // Remove not contained entries
        newEntries.removeIf(entry -> {
            var remove = !globalSelectedCollection.get().getSavegames().contains(entry);
            if (remove) {
                entry.setInactive();
            }
            return remove;
        });

        var col = globalSelectedCollection.get();
        col.getSavegames().forEach(entry -> {
            if (!newEntries.contains(entry) && entry.getState() == SavegameEntry.State.INACTIVE) {
                entry.setActive();
            }

            if (!newEntries.contains(entry) && filter.shouldShow(entry)) {
                newEntries.add(entry);
                return;
            }

            if (newEntries.contains(entry) && !filter.shouldShow(entry)) {
                newEntries.remove(entry);
            }
        });
        newEntries.sort(new Comparator<SavegameEntry<T, I>>() {
            @Override
            public int compare(SavegameEntry<T, I> o1, SavegameEntry<T, I> o2) {
                var date = o1.getDate().compareTo(o2.getDate());
                if (date != 0) {
                    return date;
                }

                try {
                    var mod = Files.getLastModifiedTime(SavegameStorage.get(game).getSavegameFile(o1))
                                   .compareTo(
                                           Files.getLastModifiedTime(SavegameStorage.get(game).getSavegameFile(o2)));
                    return mod;
                } catch (IOException e) {
                    ErrorEventFactory.fromThrowable(e).handle();
                    return 0;
                }
            }
        }.reversed());

        shownEntries.set(newEntries);

        if (globalSelectedEntry.isNotNull().get()) {
            if (!shownEntries.contains(globalSelectedEntry.get())) {
                globalSelectedEntryPropertyInternal().set(null);
            }
        }
    }

    private void updateShownCollections() {
        // Work on copy to reduce list updates
        var newCollections = FXCollections.observableArrayList(shownCollections.get());

        newCollections.removeIf(col -> {
            var remove = !SavegameStorage.get(game).getCollections().contains(col);
            if (remove) {
                if (globalSelectedCollection.get() != null && globalSelectedCollection.get().equals(col)) {
                    globalSelectedCollection.set(null);
                }
            }
            return remove;
        });

        SavegameStorage.<T, I>get(game).getCollections().forEach(col -> {
            if (!newCollections.contains(col) && filter.shouldShow(col)) {
                newCollections.add(col);
                return;
            }

            if (newCollections.contains(col) && !filter.shouldShow(col)) {
                newCollections.remove(col);
            }
        });
        newCollections.sort(Comparator.comparing(SavegameCampaign::getLastPlayed, Comparator.reverseOrder()));
        shownCollections.set(newCollections);

        updateStorageEmptyProperty();
    }

    private void updateStorageEmptyProperty() {
        int newSize = SavegameStorage.get(game).getCollections().size();
        storageEmpty.set(newSize == 0);
    }

    public ListProperty<SavegameEntry<T, I>> getShownEntries() {
        return shownEntries;
    }

    public ListProperty<SavegameCampaign<T, I>> getShownCollections() {
        return shownCollections;
    }

    private void unselectCollectionAndEntry() {
        globalSelectedEntryPropertyInternal().set(null);
        globalSelectedCampaignPropertyInternal().set(null);
        updateShownEntries();
    }

    public void selectCollectionAsync(SavegameCampaign<T, I> c) {
        if (globalSelectedCollection.isNotNull().get() && globalSelectedCollection.get().equals(c)) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            GameCacheManager.getInstance().onSelectedSavegameCollectionChange();

            if (c == null || !shownCollections.contains(c)) {
                unselectCollectionAndEntry();
                TrackEvent.debug("Unselected campaign");
                return;
            }

            // If an entry is already selected, unselect it
            if (globalSelectedEntryProperty().isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }

            globalSelectedCampaignPropertyInternal().set(c);
            updateShownEntries();
            TrackEvent.debug("Selected campaign " + c.getName());
        }, false);
    }

    public void selectEntry(SavegameEntry<T, I> e) {
        // Don't do anything if entry is not loaded yet
        if (e != null && (e.getState() == SavegameEntry.State.LOADING || e.getState() == SavegameEntry.State.UNLOADED || e.getState() == SavegameEntry.State.INACTIVE)) {
            return;
        }

        if (globalSelectedEntryPropertyInternal().isEqualTo(e).get()) {
            return;
        }

        if (e == null) {
            globalSelectedEntryPropertyInternal().set(null);
        } else {
            SavegameContext.withSavegameContext(e, ctx -> {
                if (!ctx.getCollection().equals(globalSelectedCollection.get())) {
                    return;
                }

                globalSelectedEntryPropertyInternal().set(e);
                TrackEvent.debug("Selected campaign entry " + e.getName());
            });
        }
    }

    public boolean isStorageEmpty() {
        return storageEmpty.get();
    }

    public BooleanProperty storageEmptyProperty() {
        return storageEmpty;
    }

    public class Filter {
        private final StringProperty filter = new SimpleStringProperty("");

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

        public boolean shouldShow(SavegameCampaign<T, I> col) {
            if (filter.get() == null) {
                return true;
            }

            if (col.getName().toLowerCase().contains(filter.get().toLowerCase())) {
                return true;
            }

            return col.getSavegames().stream()
                    .anyMatch(e -> e.getName().toLowerCase().contains(filter.get().toLowerCase()));
        }

        public boolean shouldShow(SavegameEntry<?, ?> entry) {
            if (filter.get() == null) {
                return true;
            }

            return entry.getName().toLowerCase().contains(filter.get().toLowerCase());
        }
    }
}
