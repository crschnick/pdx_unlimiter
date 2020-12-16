package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
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

    public static Region createTargetNode(FileImportTarget target) {
        Label name = new Label(target.getName());
        name.setTextOverrun(OverrunStyle.ELLIPSIS);
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

    public static void createTargetList(JFXListView<Node> box, List<FileImportTarget> targets) {
        box.getItems().clear();
        for (var t : targets) {
            var n = createTargetNode(t);
            box.getItems().add(n);
        }
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

        JFXListView<Node> box = new JFXListView<>();
        box.setMinWidth(400);
        createTargetList(box, watcher.getSavegames());

        Alert alert = DialogHelper.createEmptyAlert();
        alert.setTitle("Import savegames");
        alert.getDialogPane().setContent(box);
        //alert.getDialogPane().getStyleClass().add(CLASS_IMPORT_DIALOG);
        watcher.savegamesProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                createTargetList(box, n);
            });
        });
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));
        alert.showAndWait();
    }
}
