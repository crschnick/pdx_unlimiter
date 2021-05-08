package com.crschnick.pdx_unlimiter.app.gui.dialog;

import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParseResult;
import com.jfoenix.controls.JFXCheckBox;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

import java.util.Map;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CONTENT_DIALOG;


public class GuiImporter {

    public static void showResultDialog(Map<FileImportTarget, SavegameParseResult> statusMap) {
        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Import results");
        alert.setHeaderText("The import of the selected savegames has finished.");
        alert.getDialogPane().getStyleClass().add(CLASS_CONTENT_DIALOG);

        VBox list = new VBox();
        list.setSpacing(5);
        list.getChildren().add(new Label("However, there have been some issues with the savegames listed below:"));
        for (var e : statusMap.entrySet()) {
            e.getValue().visit(new SavegameParseResult.Visitor() {
                @Override
                public void invalid(SavegameParseResult.Invalid iv) {
                    list.getChildren().add(new Label("- " + e.getKey().getName() + ": " + iv.message));
                }

                @Override
                public void error(SavegameParseResult.Error er) {
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

    private static Region createTargetNode(GuiImporterState.ImportEntry entry) {
        Label name = new Label(entry.target().getName());
        name.setTextOverrun(OverrunStyle.ELLIPSIS);

        JFXCheckBox cb = new JFXCheckBox();
        cb.selectedProperty().bindBidirectional(entry.selected());
        name.setOnMouseClicked(e -> cb.setSelected(!cb.isSelected()));

        return new HBox(cb, new Label("  "), name);
    }

    public static void createImporterDialog() {
        GuiImporterState state = new GuiImporterState();

        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle("Import savegames");
        alert.getDialogPane().setContent(createContent(state));
        alert.getDialogPane().getScene().getWindow()
                .setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));


        var importType = new ButtonType("Import", ButtonBar.ButtonData.LEFT);
        alert.getButtonTypes().add(importType);
        Button importB = (Button) alert.getDialogPane().lookupButton(importType);
        importB.setOnAction(e -> {
            FileImporter.importTargets(state.getSelectedTargets());
            e.consume();
        });


        var deleteType = new ButtonType("Delete", ButtonBar.ButtonData.RIGHT);
        alert.getButtonTypes().add(deleteType);
        Button deleteB = (Button) alert.getDialogPane().lookupButton(deleteType);
        deleteB.setOnAction(e -> {
            if (GuiDialogHelper.showBlockingAlert(dAlert -> {
                dAlert.setAlertType(Alert.AlertType.CONFIRMATION);
                dAlert.setTitle("Confirm deletion");
                dAlert.setHeaderText("Do you want to delete the selected savegame(s)?");
            }).map(t -> t.getButtonData().isDefaultButton()).orElse(false)) {
                state.getSelectedTargets().forEach(FileImportTarget::delete);
            }
            e.consume();
        });

        alert.show();
    }

    private static Node createContent(GuiImporterState state) {
        VBox targets = new VBox();
        targets.getStyleClass().add("import-targets");

        JFXCheckBox cbAll = new JFXCheckBox();
        cbAll.selectedProperty().bindBidirectional(state.selectAllProperty());

        var emptyLabel = new Label("There are no savegames available to import!");

        for (var e : state.getShownTargets()) {
            var n = createTargetNode(e);
            targets.getChildren().add(n);
        }
        if (state.getShownTargets().size() == 0) {
            targets.getChildren().add(emptyLabel);
        }

        state.shownTargetsProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                targets.getChildren().clear();
                n.forEach(e -> {
                    var tn = createTargetNode(e);
                    targets.getChildren().add(tn);
                });
                if (n.size() == 0) {
                    targets.getChildren().add(emptyLabel);
                }
            });
        });

        VBox layout = new VBox();
        layout.getStyleClass().add("import-content");

        layout.getChildren().add(createFilterBar(state));
        layout.getChildren().add(new Separator());

        var sp = new ScrollPane(targets);
        sp.setFitToWidth(true);
        layout.getChildren().add(sp);
        layout.getChildren().add(new Separator());

        var selectAll = createBottomNode(cbAll);
        selectAll.getStyleClass().add("select-all");
        layout.getChildren().add(selectAll);

        return layout;
    }

    private static Region createFilterBar(GuiImporterState state) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("filter-bar");

        var fLabel = new Label("Filter:  ");
        fLabel.setAlignment(Pos.CENTER);
        box.getChildren().add(fLabel);

        TextField filter = new TextField();
        filter.textProperty().bindBidirectional(state.filterProperty());
        box.getChildren().add(filter);
        HBox.setHgrow(filter, Priority.ALWAYS);
        return box;
    }
}
