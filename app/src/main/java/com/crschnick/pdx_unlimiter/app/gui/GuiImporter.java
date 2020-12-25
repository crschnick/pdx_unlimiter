package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.LogManager;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiImporter {

    private static Region createBottomNode(CheckBox cb) {
        Label name = new Label("Select all");
        name.setOnMouseClicked(e -> cb.setSelected(!cb.isSelected()));

        HBox box = new HBox(cb, new Label("  "), name);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    public static Region createTargetNode(Set<FileImportTarget> selected, CheckBox all, FileImportTarget target) {
        Label name = new Label(target.getName());
        name.setTextOverrun(OverrunStyle.ELLIPSIS);
        JFXCheckBox cb = new JFXCheckBox();
        name.setOnMouseClicked(e -> cb.setSelected(!cb.isSelected()));
        cb.selectedProperty().addListener((c, o, n) -> {
            if (n) {
                selected.add(target);
            } else {
                selected.remove(target);
            }
        });

        all.selectedProperty().addListener((c, o, n) -> {
            cb.setSelected(n);
            if (n) {
                selected.add(target);
            } else {
                selected.remove(target);
            }
        });

        return new HBox(cb, new Label("  "), name);
    }

    public static void createTargetList(VBox box, Set<FileImportTarget> selected, List<FileImportTarget> targets, CheckBox all) {
        box.getChildren().clear();
        for (var t : targets) {
            var n = createTargetNode(selected, all, t);
            box.getChildren().add(n);
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

        VBox box = new VBox();
        box.setSpacing(5);

        JFXCheckBox cb = new JFXCheckBox();
        Set<FileImportTarget> selected = new HashSet<>();

        createTargetList(box, selected, watcher.getSavegames(), cb);
        box.getChildren().add(new Separator());
        box.getChildren().add(createBottomNode(cb));

        Alert alert = DialogHelper.createEmptyAlert();

        var importType = new ButtonType("Import", ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(importType);
        Button importB = (Button) alert.getDialogPane().lookupButton(importType);
        importB.setOnAction(e -> {
            selected.forEach(t -> FileImporter.addToImportQueue(t.toImportString()));
            e.consume();
        });

        var deleteType = new ButtonType("Delete", ButtonBar.ButtonData.RIGHT);
        alert.getButtonTypes().add(deleteType);
        Button deleteB = (Button) alert.getDialogPane().lookupButton(deleteType);
        deleteB.addEventFilter(
                ActionEvent.ACTION,
                e -> {
                    if (DialogHelper.showSavegameDeleteDialog()) {
                        selected.forEach(FileImportTarget::delete);
                    }
                    e.consume();
                }
        );

        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle("Import");
        alert.getDialogPane().setContent(box);
        alert.getDialogPane().getStyleClass().add(CLASS_IMPORT_DIALOG);
        watcher.savegamesProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                createTargetList(box, selected, n, cb);
            });
        });
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));
        alert.getDialogPane().requestFocus();
        alert.show();
    }
}
