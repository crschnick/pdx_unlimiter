package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.core.settings.SavedState;
import com.crschnick.pdx_unlimiter.app.gui.GuiPlatformHelper;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.app.util.SavegameHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class SavegameManagerState<T, I extends SavegameInfo<T>> {

    private static final Logger logger = LoggerFactory.getLogger(SavegameManagerState.class);

    private static final SavegameManagerState<?, ?> INSTANCE = new SavegameManagerState<>();
    private final SimpleObjectProperty<Game> current = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<SavegameCollection<T, I>> globalSelectedCampaign =
            new SimpleObjectProperty<>();
    private final SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntry =
            new SimpleObjectProperty<>();
    private final Filter filter = new Filter();
    private final ListProperty<SavegameCollection<T, I>> shownCollections = new SimpleListProperty<>(
            FXCollections.observableArrayList());
    private final ListProperty<SavegameEntry<T, I>> shownEntries = new SimpleListProperty<>(
            FXCollections.observableArrayList());

    private SavegameManagerState() {
        var cl = (SetChangeListener<? super SavegameEntry<T, I>>) ch -> updateShownEntries();

        globalSelectedCampaign.addListener((c, o, n) -> {
            if (o != null) {
                o.getSavegames().removeListener(cl);
                unloadCollectionAsync(o);
            }

            if (n != null) {
                n.getSavegames().addListener(cl);
            }
        });

        currentGameProperty().addListener((c,o,n) -> {
            SavedState.getInstance().setActiveGame(n);
        });
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
        });
    }

    public static void reset() {
        if (INSTANCE.current() != null) {
            INSTANCE.selectGame(null);
        }
    }

    public LocalisationHelper.Language getActiveLanguage() {
        if (current() == null) {
            return LocalisationHelper.Language.ENGLISH;
        }

        return GameInstallation.ALL.get(current()).getLanguage();
    }

    public ReadOnlyObjectProperty<SavegameCollection<T, I>> globalSelectedCampaignProperty() {
        return globalSelectedCampaign;
    }

    private SimpleObjectProperty<SavegameCollection<T, I>> globalSelectedCampaignPropertyInternal() {
        return globalSelectedCampaign;
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
        TaskExecutor.getInstance().submitTask(() -> SavegameHelper.withSavegame(e, ctx -> {
            if (globalSelectedCampaignPropertyInternal().get() != null &&
                    globalSelectedCampaignPropertyInternal().get().equals(
                            ctx.getStorage().getSavegameCollection(e))) {
                ctx.getStorage().loadEntry(e);
            }
        }), false);
    }

    public void unloadCollectionAsync(SavegameCollection<T, I> col) {
        TaskExecutor.getInstance().submitTask(() -> {
            boolean loaded = col.getSavegames().stream().anyMatch(e -> e.getInfo() != null);
            if (!loaded) {
                return;
            }

            logger.debug("Unloading collection " + col.getName());
            for (var e : col.getSavegames()) {
                e.infoProperty().set(null);
            }
        }, false);
    }

    private void updateShownEntries() {
        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            // No integration or campaign selected means no entries are shown
            if (current() == null || globalSelectedCampaign.get() == null) {
                shownEntries.clear();
                return;
            }

            // Work on copy to reduce list updates
            var newEntries = FXCollections.observableArrayList(shownEntries.get());

            // Remove not contained entries
            newEntries.removeIf(entry -> !globalSelectedCampaign.get().getSavegames().contains(entry));

            var col = globalSelectedCampaign.get();
            col.getSavegames().forEach(entry -> {
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
        });
    }

    private void updateShownCollections() {
        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            if (current() == null) {
                shownCollections.clear();
                return;
            }

            // Work on copy to reduce list updates
            var newCollections = FXCollections.observableArrayList(shownCollections.get());

            newCollections.removeIf(col -> !SavegameStorage.get(current()).getCollections().contains(col));

            SavegameStorage.<T, I>get(current()).getCollections().forEach(col -> {
                if (!newCollections.contains(col) && filter.shouldShow(col)) {
                    newCollections.add(col);
                    return;
                }

                if (newCollections.contains(col) && !filter.shouldShow(col)) {
                    newCollections.remove(col);
                }
            });
            newCollections.sort(Comparator.comparing(SavegameCollection::getLastPlayed, Comparator.reverseOrder()));
            shownCollections.set(newCollections);
        });
    }

    public ListProperty<SavegameEntry<T, I>> getShownEntries() {
        return shownEntries;
    }

    public ListProperty<SavegameCollection<T, I>> getShownCollections() {
        return shownCollections;
    }

    public Game current() {
        return current.get();
    }

    public SimpleObjectProperty<Game> currentGameProperty() {
        return current;
    }

    private void unselectCampaignAndEntry() {
        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            if (current.isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
                globalSelectedCampaignPropertyInternal().set(null);
            }
            updateShownEntries();
        });
    }

    public boolean selectGame(Game newGame) {
        if (current.get() == newGame) {
            return false;
        }

        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            unselectCampaignAndEntry();

            current.set(newGame);
            logger.debug("Selected game " +
                    (newGame != null ? newGame.getFullName() : "null"));
            updateShownCollections();
            if (newGame != null) {
                SavegameStorage.get(newGame).getCollections().addListener(
                        (SetChangeListener<? super SavegameCollection<?, ?>>) c -> {
                            updateShownCollections();
                        });
            }
        });

        return true;
    }

    public void selectCollection(SavegameCollection<T, I> c) {
        if (c == null) {
            unselectCampaignAndEntry();
            logger.debug("Unselected campaign");
            return;
        }

        if (globalSelectedCampaign.isNotNull().get() && globalSelectedCampaign.get().equals(c)) {
            return;
        }

        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            var game = SavegameHelper.getForCollection(c);

            // If we didn't change the game and an entry is already selected, unselect it
            if (!selectGame(game) && globalSelectedEntryProperty().isNotNull().get()) {
                globalSelectedEntryPropertyInternal().set(null);
            }

            globalSelectedCampaignPropertyInternal().set(c);
            updateShownEntries();
            logger.debug("Selected campaign " + c.getName());
        });
    }

    public void selectEntry(SavegameEntry<T, I> e) {
        // Don't do anything if entry is not loaded yet
        if (e != null && e.getInfo() == null) {
            return;
        }

        if (globalSelectedEntryPropertyInternal().isEqualTo(e).get()) {
            return;
        }

        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            if (e == null) {
                if (current.isNotNull().get()) {
                    globalSelectedEntryPropertyInternal().set(null);
                }
            } else {
                var game = SavegameHelper.getForSavegame(e);
                selectCollection(SavegameStorage.<T, I>get(game).getSavegameCollection(e));

                globalSelectedEntryPropertyInternal().set(e);
                logger.debug("Selected campaign entry " + e.getName());
            }
        });
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

        public boolean shouldShow(SavegameCollection<?, ?> col) {
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
