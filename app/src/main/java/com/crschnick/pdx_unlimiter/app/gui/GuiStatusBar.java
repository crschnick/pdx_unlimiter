package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.Eu4CampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.GameManager;
import com.crschnick.pdx_unlimiter.app.game.Eu4Campaign;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.effects.JFXDepthManager;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.EU4_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GameImage.imageNode;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiStatusBar {

    private static class StatusBar {
        private enum Status {
            NONE,
            SELECTED,
            INCOMPATIBLE,
            RUNNING
        }

        private Status status;
        private Pane pane;

        public StatusBar(Pane pane) {
            this.status = Status.NONE;
            this.pane = pane;
        }

        private void setRunning() {
            Region bar = createRunningBar(pane);
            if (status == Status.NONE) {
                showBar(pane, bar);
            }
            status = Status.RUNNING;
        }

        private void select() {
            if (status == Status.RUNNING) {
                return;
            }

            Region bar = null;
            if (status != Status.SELECTED &&
                    GameIntegration.current().isVersionCompatibe(GameIntegration.current().getSelectedEntry())) {
                status = Status.SELECTED;
                bar = createEntryStatusBar(pane);
            } else if (status != Status.INCOMPATIBLE){
                status = Status.INCOMPATIBLE;
                bar = createInvalidVersionStatusBar(pane);
            }

            if (bar != null) showBar(pane, bar);
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

        GameManager.getInstance().activeGameProperty().addListener((c, o, n) -> {
            if (n.isPresent()) {
                bar.setRunning();
            } else {
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

    private static Region createRunningBar(Pane pane) {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add( CLASS_STATUS_BAR);

        Label text = new Label("Europa Universalis 4 Running", imageNode(EU4_ICON, CLASS_IMAGE_ICON));
        text.getStyleClass().add( CLASS_TEXT);
        barPane.setLeft(text);

        Button b = new JFXButton("Kill");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add( CLASS_KILL);
        b.setOnAction(event -> {
            event.consume();

            hideBar(pane);
        });


        b.setAlignment(Pos.CENTER);
        barPane.setRight(b);
        return barPane;
    }

    private static Region createEntryStatusBar(Pane pane) {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add( CLASS_STATUS_BAR);

        Label text = new Label("Europa Universalis 4", imageNode(EU4_ICON, CLASS_IMAGE_ICON));
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label name = new Label("entry");
        name.setGraphic(new FontIcon());
        name.getStyleClass().add( CLASS_TEXT);
        name.getStyleClass().add( CLASS_SAVEGAME);
        barPane.setCenter(name);

        Button e = new JFXButton("Export");
        e.setGraphic(new FontIcon());
        e.getStyleClass().add( CLASS_EXPORT);
        e.setOnAction(event -> {
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

        Label text = new Label("Europa Universalis 4", imageNode(EU4_ICON, CLASS_IMAGE_ICON));
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
