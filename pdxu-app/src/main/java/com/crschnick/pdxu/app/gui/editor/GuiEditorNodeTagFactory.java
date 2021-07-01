package com.crschnick.pdxu.app.gui.editor;


import com.crschnick.pdxu.app.editor.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;

public abstract class GuiEditorNodeTagFactory {

    public abstract boolean checkIfApplicable(EditorState state, EditorSimpleNode node);

    public abstract Node create(EditorState state, EditorSimpleNode node);

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(new GuiEditorNodeTagFactory() {
        @Override
        public boolean checkIfApplicable(EditorState state, EditorSimpleNode node) {
            if (node.getBackingNode().isArray()) {
                ArrayNode ar = (ArrayNode) node.getBackingNode();
                return ar.hasKey("pattern");
            }
            return false;
        }

        @Override
        public Node create(EditorState state, EditorSimpleNode node) {
            var b = new JFXButton();
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            GuiTooltips.install(b, "Open in coat of arms preview window");
            b.setOnAction(e -> {
                var viewer = new GuiCk3CoaViewer(state, node);
                viewer.createStage();
            });
            return b;
        }
    });

    public static Optional<Node> createTag(EditorState state, EditorSimpleNode node) {
        for (var fac : FACTORIES) {
            if (fac.checkIfApplicable(state, node)) {
                return Optional.of(fac.create(state, node));
            }
        }
        return Optional.empty();
    }
}
