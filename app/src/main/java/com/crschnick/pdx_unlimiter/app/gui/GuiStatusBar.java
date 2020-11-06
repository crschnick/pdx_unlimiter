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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
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
        private JFXSnackbar snackbar;
        private Pane pane;

        public StatusBar(Pane pane) {
            this.pane = pane;
            this.snackbar = new JFXSnackbar(pane);
            this.status = Status.NONE;

            pane.widthProperty().addListener((c,o,n) -> {
                snackbar.setPrefWidth((Double) n);
            });
        }

        private void setRunning() {
            Region bar = createRunningBar(pane, snackbar);
            if (status == Status.NONE) {
                showBar(pane, bar ,snackbar);
            }
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent(bar, Duration.INDEFINITE));
            status = Status.RUNNING;
        }

        private void select() {
            if (status == Status.RUNNING) {
                return;
            }

            Status oldStatus = status;
            Region bar;
            if (GameIntegration.current().isVersionCompatibe(GameIntegration.getGlobalSelectedEntry())) {
                status = Status.SELECTED;
                bar = createEntryStatusBar(pane, snackbar);
            } else {
                status = Status.INCOMPATIBLE;
                bar = createInvalidVersionStatusBar(pane, snackbar);
            }

            if (oldStatus == Status.NONE) {
                showBar(pane, bar, snackbar);
            }
            snackbar.enqueue(new JFXSnackbar.SnackbarEvent(bar, Duration.INDEFINITE));
        }

        private void unselect() {
            if (status == Status.RUNNING) {
                return;
            }

            hideBar(pane, snackbar);
            snackbar.close();
            status = Status.NONE;
        }
    }

    public static void createStatusBar(
            ObjectProperty<Optional<Eu4Campaign>> selectedCampaign,
            ObjectProperty<Optional<Eu4CampaignEntry>> selectedEntry,
            Pane layout) {

        StatusBar bar = new StatusBar(layout);
        selectedEntry.addListener((c,o,n) -> {
            if (n.isPresent()) {
                Platform.runLater(() -> {
                    bar.select();
                });
            } else if (n.isEmpty()){
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

    public static void showBar(Pane pane, Region bar, JFXSnackbar s) {
        pane.prefHeightProperty().bind(bar.heightProperty());
        s.setPrefWidth(pane.getWidth());
    }

    private static void hideBar(Pane pane, JFXSnackbar s) {
        s.close();
        pane.maxHeightProperty().setValue(0);
    }

    private static Region createRunningBar(Pane pane, JFXSnackbar s) {

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

            hideBar(pane, s);
        });


        b.setAlignment(Pos.CENTER);
        barPane.setRight(b);
        return barPane;
    }

    private static Region createEntryStatusBar(Pane pane, JFXSnackbar s) {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add( CLASS_STATUS_BAR);

        Label text = new Label("Europa Universalis 4", imageNode(EU4_ICON, CLASS_IMAGE_ICON));
        text.getStyleClass().add( CLASS_TEXT);
        barPane.setLeft(text);

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
            hideBar(pane, s);
        });

        Button b = new JFXButton("Launch");
        b.setGraphic(new FontIcon());
        b.getStyleClass().add( CLASS_LAUNCH);
        b.setOnAction(event -> {
            GameIntegration.current().launchCampaignEntry();

            event.consume();
            hideBar(pane, s);
        });


        HBox buttons = new HBox(e, b);
        buttons.setSpacing(10);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);
        barPane.setRight(buttons);
        return barPane;
    }

    private static Region createInvalidVersionStatusBar(Pane pane, JFXSnackbar s) {
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
            hideBar(pane, s);
        });

        barPane.setRight(b);
        return barPane;
    }
}
