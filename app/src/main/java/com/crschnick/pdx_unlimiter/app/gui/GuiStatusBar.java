package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.GameAppManager;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

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
                    bar.select();
                } else {
                    bar.unselect();
                }
            });
        });

        GameAppManager.getInstance().activeGameProperty().addListener((c, o, n) -> {
            if (n != null) {
                bar.setRunning();
            } else {
                bar.stopRunning();
            }
        });

        SavegameManagerState.get().currentGameProperty().addListener((c, o, n) -> {
            bar.hide();
        });

        return pane;
    }

    private static Region createRunningBar() {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_RUNNING);

        Label text = new Label(SavegameManagerState.get().current().getName() + " (Running)",
                SavegameManagerState.get().current().getGuiFactory().createIcon());
        text.getStyleClass().add(CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label latest = new Label();
        latest.setGraphic(new FontIcon());
        latest.getStyleClass().add(CLASS_TEXT);
        latest.getStyleClass().add(CLASS_SAVEGAME);
        javafx.beans.value.ChangeListener<List<FileImportTarget>> l = (c, o, n) -> {
            Platform.runLater(() -> latest.setText("Latest: " + (n.size() > 0 ? n.get(0).getName() : "None")));
        };
        SavegameManagerState.get().current().getSavegameWatcher().savegamesProperty().addListener(l);
        l.changed(null, null,
                SavegameManagerState.get().current().getSavegameWatcher().savegamesProperty().get());
        barPane.setCenter(latest);

        Button importLatest = new JFXButton("Import");
        importLatest.setGraphic(new FontIcon());
        importLatest.getStyleClass().add(CLASS_IMPORT);
        importLatest.setOnAction(event -> {
            SavegameActions.importLatestSavegame();
            event.consume();
        });

        Button b = new JFXButton("Kill");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_KILL);
        b.setOnAction(event -> {
            event.consume();
            GameAppManager.getInstance().getActiveGame().kill();
        });

        HBox buttons = new HBox(importLatest);
        buttons.getChildren().add(b);

        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);

        barPane.setRight(buttons);
        return barPane;
    }

    private static Region createEntryStatusBar() {
        BorderPane barPane = new BorderPane();

        Label text = new Label(
                SavegameManagerState.get().current().getName(),
                SavegameManagerState.get().current().getGuiFactory().createIcon());
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label name = new Label(
                SavegameManagerState.get().globalSelectedCampaignProperty().get().getName() +
                        ", " + SavegameManagerState.get().globalSelectedEntryProperty().get().getName());
        name.setGraphic(new FontIcon());
        name.getStyleClass().add(CLASS_TEXT);
        barPane.setCenter(name);

        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        if (SavegameActions.isEntryCompatible(SavegameManagerState.get().globalSelectedEntryProperty().get())) {
            name.getStyleClass().add(CLASS_SAVEGAME);
        } else {
            barPane.getStyleClass().add(CLASS_STATUS_INCOMPATIBLE);
            name.getStyleClass().add(CLASS_ALERT);
            name.setText(name.getText() + " (Incompatible)");
        }

        Button e = new JFXButton("Export");
        e.setGraphic(new FontIcon());
        e.getStyleClass().add(CLASS_EXPORT);
        e.setOnAction(event -> {
            SavegameActions.exportCampaignEntry();

            event.consume();
            getStatusBar().hide();
        });

        Button b = new JFXButton("Launch");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_LAUNCH);
        b.setOnAction(event -> {
            SavegameActions.launchCampaignEntry();

            event.consume();
            getStatusBar().hide();
        });


        HBox buttons = new HBox(e, b);
        buttons.setSpacing(10);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);
        barPane.setRight(buttons);
        return barPane;
    }

    public static class StatusBar {
        private Status status;
        private Pane pane;

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

        private void setRunning() {
            Platform.runLater(() -> {
                Region bar = createRunningBar();
                getStatusBar().show(bar);
                status = Status.RUNNING;
            });
        }

        public void stopRunning() {
            hide();
            status = Status.NONE;
            if (SavegameManagerState.get().globalSelectedEntryProperty().isNotNull().get()) {
                select();
            }
        }

        private void select() {
            Platform.runLater(() -> {
                if (status == Status.RUNNING) {
                    return;
                }

                Region bar;
                status = Status.SELECTED;
                bar = createEntryStatusBar();

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
