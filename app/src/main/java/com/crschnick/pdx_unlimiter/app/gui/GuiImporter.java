package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiImporter {

    public static Node createTargetNode(FileImportTarget target) {
        Label name = new Label(target.getName());
        Button b = new JFXButton();
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_IMPORT);
        b.setOnAction(e -> FileImporter.addToImportQueue(target.toImportString()));

        Button del = new JFXButton();
        del.setGraphic(new FontIcon());
        del.getStyleClass().add(CLASS_DELETE);
        del.setOnAction(e -> {
            if (!Settings.getInstance().confirmDeletion() || DialogHelper.showSavegameDeleteDialog()) {
                target.delete();
            }
        });

        Region spacer = new Region();

        HBox box = new HBox(name, new Label("   "), spacer, b, del);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public static Region createTargetList(List<FileImportTarget> targets) {
        VBox box = new VBox();
        for (var t : targets) {
            box.getChildren().add(createTargetNode(t));
        }
        ScrollPane s = new ScrollPane(box);
        s.setMaxHeight(600);
        s.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        s.setFitToWidth(true);
        return s;
    }

    private static void showNoSavegamesDialog() {
        Alert alert = DialogHelper.createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("No savegames found");
        alert.setHeaderText("It seems like there are no savegames to import!");
        alert.showAndWait();
    }

    public static void createImporterDialog(SavegameWatcher watcher) {
        if (watcher.getSavegames().size() == 0) {
            showNoSavegamesDialog();
            return;
        }

        Alert alert = DialogHelper.createAlert();
        alert.setTitle("Import savegames");
        alert.getDialogPane().setContent(createTargetList(watcher.getSavegames()));
        alert.getDialogPane().getStyleClass().add(CLASS_IMPORT_DIALOG);
        watcher.savegamesProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var tl = createTargetList(n);
                alert.getDialogPane().setContent(tl);
            });
        });
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));
        alert.showAndWait();
    }
}
