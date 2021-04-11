package com.crschnick.pdx_unlimiter.app.gui.editor;


import com.crschnick.pdx_unlimiter.app.editor.EditorSimpleNode;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.gui.editor.coa.GuiCk3CoaViewer;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
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
            var b = new JFXButton("ab");
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            GuiTooltips.install(b, "Open in coat of arms designer");
            b.setOnAction(e -> GuiCk3CoaViewer.createStage(state));
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
