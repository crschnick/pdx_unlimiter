package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.Eu4CampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.GameManager;
import com.crschnick.pdx_unlimiter.app.game.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.EU4_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GameImage.imageNode;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiStatusBar {

    public static void createStatusBar(
            ObjectProperty<Optional<Eu4Campaign>> selectedCampaign,
            ObjectProperty<Optional<Eu4CampaignEntry>> selectedEntry,
            Pane layout) {

        JFXSnackbar s = new JFXSnackbar(layout);
        layout.widthProperty().addListener((c,o,n) -> {
            s.setPrefWidth((Double) n);
        });
        s.setPrefWidth(2000);
        selectedEntry.addListener((c,o,n) -> {
            if (o.isEmpty() && n.isPresent()) {
                Platform.runLater(() -> {
                    var bar = createEntryStatusBar(layout, s);
                    showBar(layout, bar, s);
                });
            } else if (o.isPresent() && n.isEmpty()){
                hideBar(layout, s);
            }
        });

        GameManager.getInstance().activeGameProperty().addListener((c, o, n) -> {
            if (n.isPresent()) {
                s.enqueue(new JFXSnackbar.SnackbarEvent(createEntryStatusBar(layout, s), Duration.INDEFINITE));
            } else {
                s.close();
                if (selectedEntry.get().isPresent()) {
                    s.enqueue(new JFXSnackbar.SnackbarEvent(createEntryStatusBar(layout, s), Duration.INDEFINITE));
                }
            }
        });
    }

    public static void showBar(Pane pane, Region bar, JFXSnackbar s) {
        s.enqueue(new JFXSnackbar.SnackbarEvent(bar, Duration.INDEFINITE));
        pane.prefHeightProperty().bind(bar.heightProperty());
        s.setPrefWidth(pane.getWidth());
    }

    private static void hideBar(Pane pane, JFXSnackbar s) {
        s.close();
        pane.maxHeightProperty().setValue(0);
    }

    private static Region createEntryStatusBar(Pane pane, JFXSnackbar s) {

        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add( CLASS_STATUS_BAR);

        Label text = new Label("Europa Universalis 4 savegame", imageNode(EU4_ICON, CLASS_IMAGE_ICON));
        text.getStyleClass().add( CLASS_TEXT);
        barPane.setLeft(text);

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
            s.close();
        });


        HBox buttons = new HBox(e, b);
        buttons.setSpacing(10);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);
        barPane.setRight(buttons);
        return barPane;
    }
}
