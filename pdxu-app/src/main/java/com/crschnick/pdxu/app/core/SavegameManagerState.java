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
import com.crschnick.pdxu.app.util.GlobalTimer;
import com.crschnick.pdxu.model.GameDate;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;

import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class SavegameManagerState<T, I extends SavegameInfo<T>> {

    @Getter
    private final Game game;

    private final SimpleObjectProperty<SavegameCampaign<T, I>> globalSelectedCollection = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntry = new SimpleObjectProperty<>();
    private final Filter filter = new Filter();
    private final ListProperty<SavegameCampaign<T, I>> shownCollections =
            new SimpleListProperty<>(FXCollections.observableList(new CopyOnWriteArrayList<>()));
    private final ListProperty<SavegameEntry<T, I>> shownEntries =
            new SimpleListProperty<>(FXCollections.observableList(new CopyOnWriteArrayList<>()));
    private final BooleanProperty storageEmpty = new SimpleBooleanProperty();
    private final ObservableValue<Boolean> gameSelected;

    public SavegameManagerState(Game game, ObservableValue<Boolean> gameSelected) {
        this.game = game;
        this.gameSelected = gameSelected;
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

        gameSelected.addListener((observable, oldValue, newValue) -> {
            unselectCollectionAndEntry();
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
        TaskExecutor.getInstance()
                .submitTask(
                        () -> SavegameContext.withSavegameContext(e, ctx -> {
                            if (globalSelectedCampaignPropertyInternal().get() != null
                                    && globalSelectedCampaignPropertyInternal()
                                            .get()
                                            .equals(ctx.getStorage().getSavegameCampaign(e))) {

                                ctx.getStorage().loadEntry(e);
                            }
                        }),
                        false);
    }

    public void unloadCollectionAsync(SavegameCampaign<T, I> col) {
        TaskExecutor.getInstance()
                .submitTask(
                        () -> {
                            TrackEvent.debug("Unloading collection " + col.getName());
                            for (var e : col.getSavegames()) {
                                e.setInactive();
                            }
                        },
                        false);
    }

    private synchronized void updateShownEntries() {
        var col = globalSelectedCollection.get();

        // No campaign selected means no entries are shown
        if (col == null) {
            if (!shownEntries.isEmpty()) {
                shownEntries.set(FXCollections.observableList(new CopyOnWriteArrayList<>()));
            }
            globalSelectedEntry.setValue(null);
            return;
        }

        for (var entry : shownEntries.get()) {
            if (!col.getSavegames().contains(entry)) {
                entry.setInactive();
            }
        }

        var lowerFilter = filter.getLowerCaseFilter();
        var newEntries = new ArrayList<SavegameEntry<T, I>>();
        for (var entry : col.getSavegames()) {
            if (entry.getState() == SavegameEntry.State.INACTIVE) {
                entry.setActive();
            }

            if (filter.shouldShow(entry, lowerFilter)) {
                newEntries.add(entry);
            }
        }

        // Only query modification times when needed and at most once per entry
        var modTimes = new HashMap<SavegameEntry<T, I>, FileTime>();
        var storage = SavegameStorage.get(game);
        newEntries.sort(Comparator.<SavegameEntry<T, I>, GameDate>comparing(SavegameEntry::getDate)
                .thenComparing(entry -> modTimes.computeIfAbsent(entry, e -> {
                    try {
                        return Files.getLastModifiedTime(storage.getSavegameFile(col, e));
                    } catch (IOException ex) {
                        ErrorEventFactory.fromThrowable(ex).handle();
                        return FileTime.fromMillis(0);
                    }
                }))
                .reversed());

        // Avoid list updates if nothing changed
        if (!newEntries.equals(shownEntries.get())) {
            shownEntries.set(FXCollections.observableList(new CopyOnWriteArrayList<>(newEntries)));
        }

        var selected = globalSelectedEntry.get();
        if (selected != null && !newEntries.contains(selected)) {
            globalSelectedEntryPropertyInternal().set(null);
        }
    }

    private synchronized void updateShownCollections() {
        var collections = SavegameStorage.<T, I>get(game).getCollections();

        var selected = globalSelectedCollection.get();
        if (selected != null && !collections.contains(selected)) {
            globalSelectedCollection.set(null);
        }

        var lowerFilter = filter.getLowerCaseFilter();
        var newCollections = new ArrayList<SavegameCampaign<T, I>>();
        for (var col : collections) {
            if (filter.shouldShow(col, lowerFilter)) {
                newCollections.add(col);
            }
        }
        newCollections.sort(Comparator.comparing(SavegameCampaign::getLastPlayed, Comparator.reverseOrder()));

        if (!newCollections.equals(shownCollections.get())) {
            shownCollections.set(FXCollections.observableList(new CopyOnWriteArrayList<>(newCollections)));
        }

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
        if (globalSelectedCollection.isNotNull().get()
                && globalSelectedCollection.get().equals(c)) {
            return;
        }

        TaskExecutor.getInstance()
                .submitTask(
                        () -> {
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
                        },
                        false);
    }

    public void selectEntry(SavegameEntry<T, I> e) {
        // Don't do anything if entry is not loaded yet
        if (e != null
                && (e.getState() == SavegameEntry.State.LOADING
                        || e.getState() == SavegameEntry.State.UNLOADED
                        || e.getState() == SavegameEntry.State.INACTIVE)) {
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
        private static final Duration UPDATE_DELAY = Duration.ofMillis(150);

        private final StringProperty filter = new SimpleStringProperty("");
        private final AtomicLong updateCounter = new AtomicLong();

        private Filter() {
            filter.addListener((c, o, n) -> {
                var id = updateCounter.incrementAndGet();
                GlobalTimer.delayAsync(
                        () -> {
                            if (updateCounter.get() != id) {
                                return;
                            }

                            SavegameManagerState.this.updateShownEntries();
                            SavegameManagerState.this.updateShownCollections();
                        },
                        UPDATE_DELAY);
            });
        }

        public String getFilter() {
            return filter.get();
        }

        public StringProperty filterProperty() {
            return filter;
        }

        private String getLowerCaseFilter() {
            var f = filter.get();
            return f != null ? f.toLowerCase() : null;
        }

        private boolean shouldShow(SavegameCampaign<T, I> col, String lowerFilter) {
            if (lowerFilter == null || lowerFilter.isEmpty()) {
                return true;
            }

            if (col.getName().toLowerCase().contains(lowerFilter)) {
                return true;
            }

            return col.getSavegames().stream().anyMatch(e -> shouldShow(e, lowerFilter));
        }

        private boolean shouldShow(SavegameEntry<?, ?> entry, String lowerFilter) {
            if (lowerFilter == null || lowerFilter.isEmpty()) {
                return true;
            }

            return entry.getName().toLowerCase().contains(lowerFilter);
        }
    }
}
