package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.gui.GuiLayoutComp;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.UserReportComp;
import com.crschnick.pdxu.app.page.PrefsPageComp;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.app.util.GlobalTimer;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class AppLayoutModel {

    private static AppLayoutModel INSTANCE;

    private final SavedState savedState;

    private final List<Entry> entries;

    private final Property<Entry> selected;

    private final ObservableList<QueueEntry> queueEntries;

    private final BooleanProperty ptbAvailable = new SimpleBooleanProperty();

    public AppLayoutModel(SavedState savedState) {
        this.savedState = savedState;
        this.entries = createEntryList();
        this.selected = new SimpleObjectProperty<>(getInitialEntry());
        this.queueEntries = FXCollections.observableArrayList();
    }

    private Entry getInitialEntry() {
        var activeGame = AppCache.getNonNull("activeGame", String.class, () -> null);
        if (activeGame != null) {
            var foundGame = GameInstallation.ALL.keySet().stream().filter(game -> game.getId().equals(activeGame)).findFirst();
            // Check if stored active game is no longer valid
            if (foundGame.isPresent()) {
                return entries.stream().filter(entry -> entry instanceof GameEntry ge && ge.game == foundGame.get()).findFirst().orElseThrow();
            } else {
                AppCache.clear("activeGame");
                return entries.getFirst();
            }
        }

        // If no active game is set, select the first one available (if existent)
        return entries.getFirst();
    }

    public void selectGame(Game game) {
        var entry = entries.stream().filter(e -> e instanceof GameEntry ge && ge.game == game).findFirst();
        if (entry.isPresent()) {
            PlatformThread.runLaterIfNeeded(() -> {
                selected.setValue(entry.get());
            });
        }
    }

    public Optional<Game> getActiveGame() {
        return selected.getValue() instanceof GameEntry ge ? Optional.of(ge.game) : Optional.empty();
    }

    public static AppLayoutModel get() {
        return INSTANCE;
    }

    public static void init() {
        var state = AppCache.getNonNull("layoutState", SavedState.class, () -> new SavedState(270, 300));
        INSTANCE = new AppLayoutModel(state);
    }

    public static void reset() {
        if (INSTANCE == null) {
            return;
        }

        AppCache.update("layoutState", INSTANCE.savedState);
        INSTANCE = null;
    }

    public synchronized void showQueueEntry(QueueEntry entry, Duration duration, boolean allowDuplicates) {
        if (!allowDuplicates && queueEntries.contains(entry)) {
            return;
        }

        queueEntries.add(entry);
        if (duration != null) {
            GlobalTimer.delay(
                    () -> {
                        synchronized (this) {
                            queueEntries.remove(entry);
                        }
                    },
                    duration);
        }
    }

    public void selectSettings() {
        PlatformThread.runLaterIfNeeded(() -> {
            var found = entries.stream().filter(entry -> entry.comp instanceof PrefsPageComp).findFirst();
            selected.setValue(found.orElseThrow());
        });
    }

    private List<Entry> createEntryList() {
        var l = new ArrayList<Entry>();
        GameInstallation.ALL.forEach((game, gameInstallation) -> {
            l.add(new GameEntry(game));
        });

        l.addAll(List.of(
                new Entry(
                        AppI18n.observable("settings"),
                        new LabelGraphic.IconGraphic("mdsmz-miscellaneous_services"),
                        new PrefsPageComp(),
                        null),
                new Entry(
                        AppI18n.observable("docs"),
                        new LabelGraphic.IconGraphic("mdi2b-book-open-variant"),
                        null,
                        () -> Hyperlinks.open(Hyperlinks.DOCS)),
                new Entry(
                        AppI18n.observable("visitGithubRepository"),
                        new LabelGraphic.IconGraphic("mdi2g-github"),
                        null,
                        () -> Hyperlinks.open(Hyperlinks.GITHUB)),
                new Entry(
                        AppI18n.observable("discord"),
                        new LabelGraphic.IconGraphic("bi-discord"),
                        null,
                        () -> Hyperlinks.open(Hyperlinks.DISCORD)),
                new Entry(
                        AppI18n.observable("feedback"),
                        new LabelGraphic.IconGraphic("mdoal-bug_report"),
                        null,
                        () -> {
                            var event = ErrorEventFactory.fromMessage("User Report");
                            if (AppLogs.get().isWriteToFile()) {
                                event.attachment(AppLogs.get().getSessionLogsDirectory());
                            }
                            UserReportComp.show(event.build());
                        })));
        return l;
    }

    @Data
    @Builder
    @Jacksonized
    public static class SavedState {

        double sidebarWidth;
        double browserConnectionsWidth;
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @NonFinal
    @EqualsAndHashCode
    public class Entry {
        ObservableValue<String> name;
        LabelGraphic icon;
        Comp<?> comp;
        Runnable action;

        public ObservableValue<String> name() {
            return name;
        }

        public LabelGraphic icon() {
            return icon;
        }

        public Comp<?> comp() {
            return comp;
        }

        public Runnable action() {
            return action;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public class GameEntry extends Entry{

        Game game;

        private GameEntry(Game game) {
            super(AppI18n.observable(game.getId()), new LabelGraphic.NodeGraphic(() -> {
                var pane = GameGuiFactory.get(game).createIcon();
                pane.setMaxWidth(24);
                pane.setMaxHeight(24);
                pane.setPrefWidth(24);
                pane.setPrefHeight(24);
                pane.setMinWidth(24);
                pane.setMinHeight(24);
                return pane;
            }), new GuiLayoutComp(new SavegameManagerState<>(game)), null);
            this.game = game;
        }
    }

    @Value
    @NonFinal
    public static class QueueEntry {

        ObservableValue<String> name;
        LabelGraphic icon;
        Runnable action;

        public void show() {
            ThreadHelper.runAsync(() -> {
                try {
                    getAction().run();
                } finally {
                    AppLayoutModel.get().getQueueEntries().remove(this);
                }
            });
        }
    }
}
