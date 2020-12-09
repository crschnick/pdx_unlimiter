package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameAppManager;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
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

import java.nio.file.Path;
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
        GameIntegration.globalSelectedEntryProperty().addListener((c, o, n) -> {
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
                bar.showImport();
            }
        });

        GameIntegration.currentGameProperty().addListener((c,o,n) -> {
            if (n != null) {
                bar.showImport();
            } else {
                bar.hide();
            }
        });

        return pane;
    }

    private static Region createImportBar() {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_IMPORT);

        Label text = new Label(GameIntegration.current().getName(),
                GameIntegration.current().getGuiFactory().createIcon());
        text.getStyleClass().add(CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label latest = new Label();
        latest.setGraphic(new FontIcon());
        latest.getStyleClass().add(CLASS_TEXT);
        latest.getStyleClass().add(CLASS_SAVEGAME);
        javafx.beans.value.ChangeListener<List<FileImportTarget>> l = (c, o, n) -> {
            Platform.runLater(() -> latest.setText(n.size() > 0 ? n.get(0).getName() : "None"));
        };
        GameIntegration.current().getInstallation().savegamesProperty().addListener(l);
        l.changed(null, null, GameIntegration.current().getInstallation().savegamesProperty().get());
        barPane.setCenter(latest);

        Button importLatest = new JFXButton("Import");
        importLatest.setGraphic(new FontIcon());
        importLatest.getStyleClass().add(CLASS_IMPORT);
        importLatest.setOnAction(event -> {
            FileImporter.importLatestSavegame();
            if (!Settings.getInstance().deleteOnImport()) {
                getStatusBar().hide();
            }
            event.consume();
        });

        HBox buttons = new HBox(importLatest);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);

        barPane.setRight(buttons);
        return barPane;
    }

    private static Region createRunningBar() {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_RUNNING);

        Label text = new Label(GameIntegration.current().getName() + " (Running)",
                GameIntegration.current().getGuiFactory().createIcon());
        text.getStyleClass().add(CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label latest = new Label();
        latest.setGraphic(new FontIcon());
        latest.getStyleClass().add(CLASS_TEXT);
        latest.getStyleClass().add(CLASS_SAVEGAME);
        javafx.beans.value.ChangeListener<List<FileImportTarget>> l = (c, o, n) -> {
            Platform.runLater(() -> latest.setText(n.size() > 0 ? n.get(0).getName() : "None"));
        };
        GameIntegration.current().getInstallation().savegamesProperty().addListener(l);
        l.changed(null, null, GameIntegration.current().getInstallation().savegamesProperty().get());
        barPane.setCenter(latest);

        Button importLatest = new JFXButton("Import");
        importLatest.setGraphic(new FontIcon());
        importLatest.getStyleClass().add(CLASS_IMPORT);
        importLatest.setOnAction(event -> {
            FileImporter.importLatestSavegame();
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
        barPane.getStyleClass().add(CLASS_STATUS_BAR);

        Label text = new Label(GameIntegration.current().getName(), GameIntegration.current().getGuiFactory().createIcon());
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label name = new Label(GameIntegration.globalSelectedEntryProperty().get().getName());
        name.setGraphic(new FontIcon());
        name.getStyleClass().add(CLASS_TEXT);
        name.getStyleClass().add(CLASS_SAVEGAME);
        barPane.setCenter(name);

        Button e = new JFXButton("Export");
        e.setGraphic(new FontIcon());
        e.getStyleClass().add(CLASS_EXPORT);
        e.setOnAction(event -> {
            GameIntegration.current().exportCampaignEntry();

            event.consume();
            getStatusBar().hide();
        });

        Button b = new JFXButton("Launch");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_LAUNCH);
        b.setOnAction(event -> {
            GameIntegration.current().launchCampaignEntry();

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

    private static Region createInvalidVersionStatusBar() {
        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_INCOMPATIBLE);

        Label text = new Label(GameIntegration.current().getName(), GameIntegration.current().getGuiFactory().createIcon());
        text.getStyleClass().add(CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label name = new Label("Incompatible");
        name.setGraphic(new FontIcon());
        name.getStyleClass().add(CLASS_TEXT);
        name.getStyleClass().add(CLASS_ALERT);
        barPane.setCenter(name);
        BorderPane.setAlignment(name, Pos.CENTER);

        Button b = new JFXButton("Launch");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_LAUNCH);
        b.setOnAction(event -> {
            event.consume();
            if (GameIntegration.current().getGuiFactory().displayIncompatibleWarning(
                    GameIntegration.globalSelectedEntryProperty().get())) {
                GameIntegration.current().launchCampaignEntry();
            }
        });

        barPane.setRight(b);
        BorderPane.setAlignment(b, Pos.CENTER);
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
            pane.getChildren().setAll(bar);
            bar.prefWidthProperty().bind(pane.widthProperty());
        }

        private void hide() {
            pane.getChildren().clear();
        }

        private void setRunning() {
            Platform.runLater(() -> {
                Region bar = createRunningBar();
                getStatusBar().show(bar);
                status = Status.RUNNING;
            });
        }

        public void showImport() {
            Platform.runLater(() -> {
                Region bar = createImportBar();
                show(bar);
                status = Status.IMPORT;
            });
        }

        private void select() {
            if (status == Status.RUNNING) {
                return;
            }

            Region bar;
            if (GameIntegration.current().isEntryCompatible(GameIntegration.globalSelectedEntryProperty().get())) {
                status = Status.SELECTED;
                bar = createEntryStatusBar();
            } else {
                status = Status.INCOMPATIBLE;
                bar = createInvalidVersionStatusBar();
            }

            show(bar);
        }

        private void unselect() {
            if (status == Status.RUNNING) {
                return;
            }

            hide();
            status = Status.NONE;
        }

        private enum Status {
            NONE,
            SELECTED,
            INCOMPATIBLE,
            RUNNING,
            IMPORT
        }
    }
}
