package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CONTENT_DIALOG;


public class GuiImporter {

    public static void showResultDialog(Map<FileImportTarget, SavegameParser.Status> statusMap) {
        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle("Import results");
        alert.setHeaderText("The import of the selected savegames has finished.");
        alert.getDialogPane().getStyleClass().add(CLASS_CONTENT_DIALOG);

        VBox list = new VBox();
        list.setSpacing(5);
        list.getChildren().add(new Label("However, there have been some issues with the savegames listed below:"));
        for (var e : statusMap.entrySet()) {
            e.getValue().visit(new SavegameParser.StatusVisitor<>() {
                @Override
                public void invalid(SavegameParser.Invalid iv) {
                    list.getChildren().add(new Label("- " + e.getKey().getName() + ": " + iv.message));
                }

                @Override
                public void error(SavegameParser.Error er) {
                    list.getChildren().add(new Label("- " + e.getKey().getName() + ": " +
                            er.error.getMessage() + " (error)"));
                }
            });
        }
        alert.getDialogPane().setContent(list);

        // Only show dialog if there were issues
        if (statusMap.size() > 0) {
            alert.show();
        }
    }

    private static Region createBottomNode(CheckBox cb) {
        Label name = new Label("Select all");
        name.setOnMouseClicked(e -> cb.setSelected(!cb.isSelected()));

        HBox box = new HBox(cb, new Label("  "), name);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Region createTargetNode(Set<FileImportTarget> selected, CheckBox all, FileImportTarget target) {
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

    private static void showNoSavegamesDialog() {
        Alert alert = GuiDialogHelper.createAlert();
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

        Set<FileImportTarget> selected = new HashSet<>();

        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle("Import");
        alert.getDialogPane().setContent(create(watcher.getSavegames(), selected));
        watcher.savegamesProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                // Clear the selected savegames!
                selected.clear();
                alert.getDialogPane().setContent(create(n, selected));
            });
        });
        alert.getDialogPane().getScene().getWindow().setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));

        var importType = new ButtonType("Import", ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(importType);
        Button importB = (Button) alert.getDialogPane().lookupButton(importType);
        importB.setOnAction(e -> {
            FileImporter.importTargets(selected);
            e.consume();
        });

        var deleteType = new ButtonType("Delete", ButtonBar.ButtonData.RIGHT);
        alert.getButtonTypes().add(deleteType);
        alert.show();
    }

    private static Node create(List<FileImportTarget> savegames, Set<FileImportTarget> selected) {
        VBox targets = new VBox();
        targets.setSpacing(5);
        JFXCheckBox cbAll = new JFXCheckBox();

        for (var t : savegames) {
            var n = createTargetNode(selected, cbAll, t);
            targets.getChildren().add(n);
        }

        VBox layout = new VBox();
        layout.setSpacing(8);
        var sp = new ScrollPane(targets);
        sp.setFitToWidth(true);
        sp.setPrefViewportWidth(250);
        sp.setPrefViewportHeight(500);
        layout.getChildren().add(sp);
        layout.getChildren().add(new Separator());
        layout.getChildren().add(createBottomNode(cbAll));
        return layout;
    }
}
