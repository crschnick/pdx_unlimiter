package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.gui.GuiPlatformHelper;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.SavegameInfoHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Optional;

public class SavegameManagerState<T, I extends SavegameInfo<T>> {

    private static final Logger logger = LoggerFactory.getLogger(SavegameManagerState.class);

    private static final SavegameManagerState<?, ?> INSTANCE = new SavegameManagerState<Object, SavegameInfo<Object>>();
    private final SimpleObjectProperty<GameIntegration<?, ? extends SavegameInfo<?>>> current = new SimpleObjectProperty<>();
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
        var cl = (SetChangeListener<? super SavegameEntry<T, I>>) ch -> {
            updateShownEntries();
        };

        globalSelectedCampaign.addListener((c, o, n) -> {
            if (o != null) {
                o.getSavegames().removeListener(cl);
                unloadCollectionAsync(o);
            }

            if (n != null) {
                n.getSavegames().addListener(cl);
            }
        });
    }

    @SuppressWarnings("unchecked")
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
        return globalSelectedEntry;
    }

    private SimpleObjectProperty<SavegameEntry<T, I>> globalSelectedEntryPropertyInternal() {
        return globalSelectedEntry;
    }

    public Filter getFilter() {
        return filter;
    }

    public void loadEntryAsync(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameInfoHelper.doWithIntegration(e, gi -> {
                if (globalSelectedCampaignPropertyInternal().get() != null &&
                        globalSelectedCampaignPropertyInternal().get().equals(
                                gi.getSavegameStorage().getSavegameCollection(e))) {
                    gi.getSavegameStorage().loadEntry(e);
                }
            });
        }, false);
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

            newCollections.removeIf(col -> !current().getSavegameStorage().getCollections().contains(col));

            current().getSavegameStorage().getCollections().forEach(col -> {
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

    public GameIntegration<T, I> current() {
        return (GameIntegration<T, I>) current.get();
    }

    public <G extends GameIntegration<T, I>> SimpleObjectProperty<G> currentGameProperty() {
        return (SimpleObjectProperty<G>) current;
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

    public boolean selectIntegration(GameIntegration<?, ?> newInt) {
        if (current.get() == newInt) {
            return false;
        }

        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {
            unselectCampaignAndEntry();

            current.set(newInt);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected integration " + (newInt != null ? newInt.getName() : "null"));
            updateShownCollections();
            if (newInt != null) {
                newInt.getSavegameStorage().getCollections().addListener(
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
            LoggerFactory.getLogger(GameIntegration.class).debug("Unselected campaign");
            return;
        }

        if (globalSelectedCampaign.isNotNull().get() && globalSelectedCampaign.get().equals(c)) {
            return;
        }

        GuiPlatformHelper.doWhilePlatformIsPaused(() -> {

            Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                    .filter(i -> i.getSavegameStorage().getCollections().contains(c))
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
                Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                        .filter(i -> i.getSavegameStorage().contains(e))
                        .findFirst()
                        .map(v -> (GameIntegration<T, I>) v);
                gi.ifPresentOrElse(v -> {
                    selectCollection(v.getSavegameStorage().getSavegameCollection(e));

                    globalSelectedEntryPropertyInternal().set(e);
                    LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign entry " + e.getName());
                }, () -> {
                    LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign entry " + e.getName());
                });
            }
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
