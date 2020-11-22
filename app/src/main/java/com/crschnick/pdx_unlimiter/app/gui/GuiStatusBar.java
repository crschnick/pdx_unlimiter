package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameApp;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.game.GameAppManager;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;


import java.nio.file.Path;
import java.util.List;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.EU4_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GameImage.imageNode;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiStatusBar {

    private static class StatusBar {
        private enum Status {
            NONE,
            SELECTED,
            INCOMPATIBLE,
            RUNNING,
            STOPPED
        }

        private Status status;
        private Pane pane;

        public StatusBar(Pane pane) {
            this.status = Status.NONE;
            this.pane = pane;
        }

        private void setRunning() {
            Platform.runLater(() -> {
                Region bar = createRunningBar(true);
                showBar(pane, bar);
                status = Status.RUNNING;
            });
        }

        private void stop() {
            Platform.runLater(() -> {
                Region bar = createRunningBar(false);
                showBar(pane, bar);
                status = Status.STOPPED;
            });
        }

        private void select() {
            if (status == Status.RUNNING) {
                return;
            }

            Region bar = null;
            if (GameIntegration.current().isVersionCompatible(GameIntegration.globalSelectedEntryProperty().get())) {
                status = Status.SELECTED;
                bar = createEntryStatusBar(pane);
            } else {
                status = Status.INCOMPATIBLE;
                bar = createInvalidVersionStatusBar(pane);
            }

            showBar(pane, bar);
        }

        private void unselect() {
            if (status == Status.RUNNING) {
                return;
            }

            hideBar(pane);
            status = Status.NONE;
        }
    }

    public static void createStatusBar(Pane layout) {

        StatusBar bar = new StatusBar(layout);
        GameIntegration.globalSelectedEntryProperty().addListener((c,o,n) -> {
            if (n != null) {
                Platform.runLater(() -> {
                    bar.select();
                });
            } else {
                bar.unselect();
            }
        });

        GameAppManager.getInstance().activeGameProperty().addListener((c, o, n) -> {
            if (n != null) {
                bar.setRunning();
            } else {
                bar.stop();
            }
        });
    }

    public static void showBar(Pane pane, Region bar) {
        pane.getChildren().setAll(bar);
        bar.setPrefWidth(pane.getWidth());
    }

    private static void hideBar(Pane pane) {
        pane.getChildren().clear();
    }

    private static Region createRunningBar(boolean running) {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add( CLASS_STATUS_BAR);
        if (running) {
            barPane.getStyleClass().add(CLASS_STATUS_RUNNING);
        } else {
            barPane.getStyleClass().add(CLASS_STATUS_STOPPED);
        }

        Label text = new Label(GameIntegration.current().getName() + " (" + (running ? "Running" : "Stopped") + ")",
                GameIntegration.current().getGuiFactory().createIcon());
        text.getStyleClass().add( CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label latest = new Label();
        latest.setGraphic(new FontIcon());
        latest.getStyleClass().add( CLASS_TEXT);
        latest.getStyleClass().add( CLASS_SAVEGAME);
        javafx.beans.value.ChangeListener<List<Path>> l = (c, o, n) -> {
            Platform.runLater(() -> latest.setText(n.size() > 0 ? n.get(0).getFileName().toString(): "None"));
        };
        GameIntegration.current().getInstallation().savegamesProperty().addListener(l);
        l.changed(null, null, GameIntegration.current().getInstallation().savegamesProperty().get());
        barPane.setCenter(latest);

        Button importLatest = new JFXButton("Import");
        importLatest.setGraphic(new FontIcon());
        importLatest.getStyleClass().add( CLASS_IMPORT);
        importLatest.setOnAction(event -> {
            if (GameIntegration.current().getInstallation().getSavegames().size() == 0) {
                return;
            }

            FileImporter.importFile(GameIntegration.current().getInstallation().getSavegames().get(0));
            event.consume();
        });

        Button b = new JFXButton("Kill");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add( CLASS_KILL);
        b.setOnAction(event -> {
            event.consume();
            GameAppManager.getInstance().getActiveGame().kill();
        });

        HBox buttons = new HBox(importLatest);
        if (running) {
            buttons.getChildren().add(b);
        }
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);

        barPane.setRight(buttons);
        return barPane;
    }

    private static Region createEntryStatusBar(Pane pane) {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add( CLASS_STATUS_BAR);

        Label text = new Label(GameIntegration.current().getName(), GameIntegration.current().getGuiFactory().createIcon());
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label name = new Label(GameIntegration.globalSelectedEntryProperty().get().getName());
        name.setGraphic(new FontIcon());
        name.getStyleClass().add( CLASS_TEXT);
        name.getStyleClass().add( CLASS_SAVEGAME);
        barPane.setCenter(name);

        Button e = new JFXButton("Export");
        e.setGraphic(new FontIcon());
        e.getStyleClass().add( CLASS_EXPORT);
        e.setOnAction(event -> {
            GameIntegration.current().exportCampaignEntry();
            GameIntegration.current().launchGame();

            event.consume();
            hideBar(pane);
        });

        Button b = new JFXButton("Launch");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add( CLASS_LAUNCH);
        b.setOnAction(event -> {
            GameIntegration.current().launchCampaignEntry();

            event.consume();
            hideBar(pane);
        });


        HBox buttons = new HBox(e, b);
        buttons.setSpacing(10);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);
        barPane.setRight(buttons);
        return barPane;
    }

    private static Region createInvalidVersionStatusBar(Pane pane) {
        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_INCOMPATIBLE);

        Label text = new Label(GameIntegration.current().getName(), GameIntegration.current().getGuiFactory().createIcon());
        text.getStyleClass().add( CLASS_TEXT);
        barPane.setLeft(text);

        Label name = new Label("Incompatible version");
        name.setGraphic(new FontIcon());
        name.getStyleClass().add( CLASS_TEXT);
        name.getStyleClass().add(CLASS_ALERT);
        barPane.setCenter(name);

        Button b = new JFXButton("Close");
        b.setGraphic(new FontIcon());
        b.setOnAction(event -> {
            event.consume();
            hideBar(pane);
        });

        barPane.setRight(b);
        return barPane;
    }
}
