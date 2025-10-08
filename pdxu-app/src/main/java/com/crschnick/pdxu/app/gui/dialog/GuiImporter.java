package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.core.window.AppSideWindow;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.FileImportTarget;
import com.crschnick.pdxu.app.savegame.FileImporter;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Map;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_CONTENT_DIALOG;


public class GuiImporter {

    public static void showResultDialog(Map<FileImportTarget, SavegameParseResult> statusMap) {
        Alert alert = AppSideWindow.createEmptyAlert();
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
        Label name = new Label();
        name.textProperty().bind(AppI18n.observable("selectAll"));
        name.setOnMouseClicked(e -> cb.setSelected(!cb.isSelected()));

        HBox box = new HBox(cb, new Label("  "), name);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private static Region createTargetNode(GuiImporterState.ImportEntry entry) {
        Label name = new Label(entry.target().getName());
        name.setTextOverrun(OverrunStyle.ELLIPSIS);

        var cb = new CheckBox();
        cb.selectedProperty().bindBidirectional(entry.selected());
        name.setOnMouseClicked(e -> cb.setSelected(!cb.isSelected()));

        return new HBox(cb, new Label("  "), name);
    }

    public static void createImporterDialog(Game game) {
        GuiImporterState state = new GuiImporterState(game);

        var modal = ModalOverlay.of("importSavegames", Comp.of(() -> createContent(state)));
        modal.addButton(new ModalButton("delete", () -> {
            var confirm = AppDialog.confirm("deleteSavegames");
            if (confirm) {
                state.getSelectedTargets().forEach(FileImportTarget::delete);
            }
        }, false, false));
        modal.addButtonBarComp(Comp.hspacer());
        modal.addButton(new ModalButton("import", () -> {
            FileImporter.importTargets(state.getSelectedTargets());
        }, true, true));
        modal.show();
    }

    private static Region createContent(GuiImporterState state) {
        VBox targets = new VBox();
        targets.getStyleClass().add("import-targets");

        var cbAll = new CheckBox();
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
        box.setSpacing(9);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("filter-bar");

        var fLabel = new Label();
        fLabel.textProperty().bind(AppI18n.observable("filter"));
        fLabel.setAlignment(Pos.CENTER);
        box.getChildren().add(fLabel);

        TextField filter = new TextField();
        filter.textProperty().bindBidirectional(state.filterProperty());
        box.getChildren().add(filter);
        HBox.setHgrow(filter, Priority.ALWAYS);
        return box;
    }
}
