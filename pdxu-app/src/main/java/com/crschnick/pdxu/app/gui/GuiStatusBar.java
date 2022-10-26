package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameAppManager;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.*;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public class GuiStatusBar {

    private static StatusBar bar;

    public static StatusBar getStatusBar() {
        return bar;
    }

    public static Pane createStatusBar() {
        Pane pane = new Pane();
        bar = new StatusBar(pane);
        SavegameManagerState.get().globalSelectedEntryProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                if (n != null) {
                    bar.select(n);
                } else {
                    bar.unselect();
                }
            });
        });

        GameAppManager.getInstance().activeGameProperty().addListener((c, o, n) -> {
            if (n != null) {
                bar.setRunning(n.getGame());
            } else {
                bar.stopRunning();
            }
        });
        var g = GameAppManager.getInstance().activeGameProperty().get();
        if (g != null) {
            bar.setRunning(g.getGame());
        }

        return pane;
    }

    private static Region createRunningBar(Game g) {
        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_RUNNING);

        Label text = new Label(g.getTranslatedFullName() + " (" + PdxuI18n.get("RUNNING") + ")",
                GameGuiFactory.get(g).createIcon());
        text.getStyleClass().add(CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label latest = new Label();
        latest.setGraphic(new FontIcon());
        latest.getStyleClass().add(CLASS_TEXT);
        latest.getStyleClass().add(CLASS_SAVEGAME);

        var watcher = SavegameWatcher.ALL.get(g);
        javafx.beans.value.ChangeListener<List<FileImportTarget.StandardImportTarget>> l = (c, o, n) -> {
            Platform.runLater(() -> {
                var name = watcher.getLatest()
                        .map(FileImportTarget.StandardImportTarget::getName)
                        .orElse(PdxuI18n.get("NONE"));
                latest.setText(PdxuI18n.get("LATEST") + ": " + name);
            });
        };
        watcher.savegamesProperty().addListener(l);
        l.changed(null, null, watcher.savegamesProperty().get());
        barPane.setCenter(latest);

        Button importLatest = new Button(PdxuI18n.get("IMPORT"));
        importLatest.setGraphic(new FontIcon());
        importLatest.getStyleClass().add(CLASS_IMPORT);
        importLatest.setOnAction(event -> {
            GameAppManager.getInstance().importLatest();
            event.consume();
        });

        Button b = new Button(PdxuI18n.get("KILL"));
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_KILL);
        b.setOnAction(event -> {
            GameAppManager.getInstance().kill();
            event.consume();
        });

        HBox buttons = new HBox(importLatest);
        buttons.setSpacing(5);
        buttons.getChildren().add(b);

        {
            Button help = new Button();
            help.setGraphic(new FontIcon());
            help.getStyleClass().add("help-button");
            help.setOnAction(event -> {
                ThreadHelper.browse(Hyperlinks.SAVESCUM_GUIDE);
                event.consume();
            });
            buttons.getChildren().add(help);
        }

        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);

        barPane.setRight(buttons);
        return barPane;
    }

    private static <T, I extends SavegameInfo<T>> Region createEntryStatusBar(SavegameEntry<T, I> e) {
        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);
        barPane.setRight(buttons);

        SavegameContext.withSavegameContext(e, ctx -> {
            Label text = new Label(
                    ctx.getStorage().getEntryName(e),
                    ctx.getGuiFactory().createIcon());
            text.getStyleClass().add("text");
            barPane.setLeft(text);
            BorderPane.setAlignment(text, Pos.CENTER);

            switch (SavegameCompatibility.determineForEntry(e)) {
                case INCOMPATIBLE -> barPane.getStyleClass().add(CLASS_STATUS_INCOMPATIBLE);
                case UNKNOWN -> barPane.getStyleClass().add("status-compatible-unknown");
            }

            {
                Button export = new Button(PdxuI18n.get("EXPORT"));
                export.setGraphic(new FontIcon());
                export.getStyleClass().add(CLASS_EXPORT);
                export.setOnAction(event -> {
                    SavegameActions.exportSavegame(e);
                    SavegameManagerState.<T, I>get().selectEntry(null);

                    event.consume();
                    getStatusBar().hide();
                });
                buttons.getChildren().add(export);
            }

            if (e.getInfo() != null && ctx.getInstallation().getDist().supportsLauncher()) {
                Button launch = new Button(PdxuI18n.get("START_LAUNCHER"));
                launch.setGraphic(new FontIcon());
                launch.getStyleClass().add("launcher-button");
                launch.setOnAction(event -> {
                    GameDistLauncher.startLauncherWithContinueGame(e);
                    SavegameManagerState.<T, I>get().selectEntry(null);

                    event.consume();
                    getStatusBar().hide();
                });
                buttons.getChildren().add(launch);
            }

            if (e.getInfo() != null && ctx.getInstallation().getDist().supportsDirectLaunch()) {
                ButtonBase launch;
                if (ctx.getInstallation().getType().debugModeSwitch().isPresent()) {
                    var splitButton = new SplitMenuButton();
                    splitButton.setText(PdxuI18n.get("CONTINUE_GAME"));

                    var debugItem = new MenuItem(PdxuI18n.get("DEBUG_MODE"));
                    debugItem.setGraphic(new FontIcon());
                    debugItem.getStyleClass().add("continue-button");
                    debugItem.setOnAction(event -> {
                        GameDistLauncher.continueSavegame(e, true);
                        SavegameManagerState.<T, I>get().selectEntry(null);
                        event.consume();
                        getStatusBar().hide();
                    });
                    splitButton.getItems().add(debugItem);
                    launch = splitButton;
                } else {
                    launch = new Button(PdxuI18n.get("CONTINUE_GAME"));
                }

                launch.setGraphic(new FontIcon());
                launch.getStyleClass().add("continue-button");
                launch.setOnAction(event -> {
                    GameDistLauncher.continueSavegame(e, false);
                    SavegameManagerState.<T, I>get().selectEntry(null);
                    event.consume();
                    getStatusBar().hide();
                });
                buttons.getChildren().add(launch);
            }

            {
                Button help = new Button();
                help.setGraphic(new FontIcon());
                help.getStyleClass().add("help-button");
                help.setOnAction(event -> {
                    ThreadHelper.browse(Hyperlinks.LAUNCH_GUIDE);
                    event.consume();
                });
                buttons.getChildren().add(help);
            }
        });

        return barPane;
    }

    public static class StatusBar {
        private final Pane pane;
        private Status status;

        public StatusBar(Pane pane) {
            this.status = Status.NONE;
            this.pane = pane;
        }

        private void show(Region bar) {
            Platform.runLater(() -> {
                pane.getChildren().setAll(bar);
                bar.prefWidthProperty().bind(pane.widthProperty());
            });
        }

        private void hide() {
            Platform.runLater(() -> {
                pane.getChildren().clear();
            });
        }

        private void setRunning(Game g) {
            // Create node before Platform thread to avoid async issues!
            Region bar = createRunningBar(g);
            Platform.runLater(() -> {
                show(bar);
                status = Status.RUNNING;
            });
        }

        public void stopRunning() {
            hide();
            status = Status.NONE;
            var sel = SavegameManagerState.get().globalSelectedEntryProperty().get();
            if (sel != null) {
                select(sel);
            }
        }

        private <T, I extends SavegameInfo<T>> void select(SavegameEntry<T, I> e) {
            Region bar = createEntryStatusBar(e);
            Platform.runLater(() -> {
                if (status == Status.RUNNING) {
                    return;
                }

                status = Status.SELECTED;
                show(bar);
            });
        }

        private void unselect() {
            Platform.runLater(() -> {
                if (status == Status.RUNNING) {
                    return;
                }

                hide();
                status = Status.NONE;
            });
        }

        private enum Status {
            NONE,
            SELECTED,
            RUNNING
        }
    }
}
