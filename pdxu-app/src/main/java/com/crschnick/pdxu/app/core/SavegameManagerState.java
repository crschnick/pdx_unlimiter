package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.settings.SavedState;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SavegameManagerState<T, I extends SavegameInfo<T>> {

    private static final Logger logger = LoggerFactory.getLogger(SavegameManagerState.class);

    private static final SavegameManagerState<?, ?> INSTANCE = new SavegameManagerState<>();
    private final SimpleObjectProperty<Game> current = new SimpleObjectProperty<>();
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

    private SavegameManagerState() {
        addShownContentChangeListeners();
    }

    @SuppressWarnings("unchecked")
    public static <T, I extends SavegameInfo<T>> SavegameManagerState<T, I> get() {
        return (SavegameManagerState<T, I>) INSTANCE;
    }

    public static void init() {
        SavedState s = SavedState.getInstance();
        if (s.getActiveGame() != null) {
            // Check if stored active game is no longer valid
            if (!GameInstallation.ALL.containsKey(s.getActiveGame())) {
                SavedState.getInstance().setActiveGame(null);
            } else {
                INSTANCE.selectGame(s.getActiveGame());
                return;
            }
        }

        // If no active game is set, select the first one available (if existent)
        GameInstallation.ALL.entrySet().stream().findFirst().ifPresent(e -> {
            INSTANCE.selectGame(e.getKey());
            SavedState.getInstance().setActiveGame(e.getKey());
        });
    }

    public static void reset() {
        if (INSTANCE.current() != null) {
            INSTANCE.selectGame(null);
        }
    }

    private void addShownContentChangeListeners() {
        var colListener = (SetChangeListener<? super SavegameCampaign<Object, SavegameInfo<Object>>>) c -> {
            updateShownCollections();
            updateShownEntries();
        };
        current.addListener((c, o, n) -> {
            if (o != null) {
                SavegameStorage.get(o).getCollections().removeListener(colListener);
            }
            if (n != null) {
                SavegameStorage.get(n).getCollections().addListener(colListener);
            }
        });


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

    public void onGameChange(Consumer<Game> change) {
        currentGameProperty().addListener((c, o, n) -> {
            change.accept(n);
        });
        change.accept(current());
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
            logger.debug("Unloading collection " + col.getName());
            for (var e : col.getSavegames()) {
                e.setInactive();
            }
        }, false);
    }

    private void updateShownEntries() {
        // No integration or campaign selected means no entries are shown
        if (current() == null || globalSelectedCollection.get() == null) {
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
        newEntries.sort(Comparator.comparing(SavegameEntry::getDate, Comparator.reverseOrder()));

        shownEntries.set(newEntries);

        if (globalSelectedEntry.isNotNull().get()) {
            if (!shownEntries.contains(globalSelectedEntry.get())) {
                globalSelectedEntryPropertyInternal().set(null);
            }
        }
    }

    private void updateShownCollections() {
        if (current() == null) {
            shownCollections.clear();
            return;
        }

        // Work on copy to reduce list updates
        var newCollections = FXCollections.observableArrayList(shownCollections.get());

        newCollections.removeIf(col -> {
            var remove = !SavegameStorage.get(current()).getCollections().contains(col);
            if (remove) {
                if (globalSelectedCollection.get() != null && globalSelectedCollection.get().equals(col)) {
                    globalSelectedCollection.set(null);
                }
            }
            return remove;
        });

        SavegameStorage.<T, I>get(current()).getCollections().forEach(col -> {
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
        if (current() == null) {
            storageEmpty.set(false);
            return;
        }

        int newSize = SavegameStorage.get(current()).getCollections().size();
        storageEmpty.set(newSize == 0);
    }

    public ListProperty<SavegameEntry<T, I>> getShownEntries() {
        return shownEntries;
    }

    public ListProperty<SavegameCampaign<T, I>> getShownCollections() {
        return shownCollections;
    }

    public Game current() {
        return current.get();
    }

    public SimpleObjectProperty<Game> currentGameProperty() {
        return current;
    }

    private void unselectCollectionAndEntry() {
        if (current.isNotNull().get()) {
            globalSelectedEntryPropertyInternal().set(null);
            globalSelectedCampaignPropertyInternal().set(null);
        }
        updateShownEntries();
    }

    private void selectGame(Game newGame) {
        if (current.get() == newGame) {
            return;
        }

        unselectCollectionAndEntry();
        current.set(newGame);
        logger.debug("Selected game " + (newGame != null ? newGame.getInstallationName() : "null"));
        updateShownCollections();
    }

    public void selectGameAsync(Game newGame) {
        if (current.get() == newGame) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            CacheManager.getInstance().onSelectedGameChange();
            selectGame(newGame);
        }, false);
    }

    public void selectCollectionAsync(SavegameCampaign<T, I> c) {
        if (globalSelectedCollection.isNotNull().get() && globalSelectedCollection.get().equals(c)) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            CacheManager.getInstance().onSelectedSavegameCollectionChange();

            if (c == null || !shownCollections.contains(c)) {
                unselectCollectionAndEntry();
                logger.debug("Unselected campaign");
                return;
            }

            // If an entry is already selected, unselect it
            if (globalSelectedEntryProperty().isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }

            globalSelectedCampaignPropertyInternal().set(c);
            updateShownEntries();
            logger.debug("Selected campaign " + c.getName());
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
            if (current.isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }
        } else {
            SavegameContext.withSavegameContext(e, ctx -> {
                if (!ctx.getCollection().equals(globalSelectedCollection.get())) {
                    return;
                }

                globalSelectedEntryPropertyInternal().set(e);
                logger.debug("Selected campaign entry " + e.getName());
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
            if (col.getName().toLowerCase().contains(filter.get().toLowerCase())) {
                return true;
            }

            return col.getSavegames().stream()
                    .anyMatch(e -> e.getName().toLowerCase().contains(filter.get().toLowerCase()));
        }

        public boolean shouldShow(SavegameEntry<?, ?> entry) {
            return entry.getName().toLowerCase().contains(filter.get().toLowerCase());
        }
    }
}
