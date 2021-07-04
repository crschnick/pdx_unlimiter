package com.crschnick.pdxu.app.gui.editor;

import com.crschnick.pdxu.app.gui.dialog.GuiImporterState;
import javafx.scene.Node;

public class GuiEditorFileContextDialog {

    public static void show() {

    }

    private static Node createContent(GuiImporterState state) {
//        VBox targets = new VBox();
//        targets.getStyleClass().add("import-targets");
//
//        JFXCheckBox cbAll = new JFXCheckBox();
//        cbAll.selectedProperty().bindBidirectional(state.selectAllProperty());
//
//        for (var e : state.getShownTargets()) {
//            var n = createTargetNode(e);
//            targets.getChildren().add(n);
//        }
//
//        state.shownTargetsProperty().addListener((c, o, n) -> {
//            Platform.runLater(() -> {
//                targets.getChildren().clear();
//                n.forEach(e -> {
//                    var tn = createTargetNode(e);
//                    targets.getChildren().add(tn);
//                });
//            });
//        });
//
//        VBox layout = new VBox();
//        layout.getStyleClass().add("import-content");
//
//        layout.getChildren().add(createFilterBar(state));
//        layout.getChildren().add(new Separator());
//
//        var sp = new ScrollPane(targets);
//        sp.setFitToWidth(true);
//        layout.getChildren().add(sp);
//        layout.getChildren().add(new Separator());
//
//        var selectAll = createBottomNode(cbAll);
//        selectAll.getStyleClass().add("select-all");
//        layout.getChildren().add(selectAll);
//
//        return layout;
        return null;
    }
}
