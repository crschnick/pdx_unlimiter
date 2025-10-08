package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.app.comp.base.TooltipHelper;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.editor.node.EditorSimpleNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.model.GameColor;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class GuiEditorNode {

    static Region createValueDisplay(EditorNode n, EditorState state) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setFillHeight(true);

        if (n.isReal() && ((EditorRealNode) n).getBackingNode().isValue()) {
            var tf = new TextField(((EditorSimpleNode) n).getBackingNode().getString());
            tf.setAlignment(Pos.CENTER);
            tf.textProperty().addListener((c, o, ne) -> {
                ((EditorSimpleNode) n).updateText(ne);
                state.onTextChanged();
            });
            tf.setEditable(state.isEditable());
            box.getChildren().add(tf);
            HBox.setHgrow(tf, Priority.ALWAYS);
        } else if (n.isReal() && ((EditorRealNode) n).getBackingNode().isTagged() &&
                ((EditorRealNode) n).getBackingNode().describe().getValueType().equals(Node.ValueType.COLOR)) {
            var picker = new ColorPicker(ColorHelper.fromGameColor(GameColor.fromColorNode(
                    ((EditorSimpleNode) n).getBackingNode())));
            picker.valueProperty().addListener((c, o, ne) -> {
                ((EditorSimpleNode) n).updateColor(ne);
                state.onColorChanged();
            });
            picker.setEditable(state.isEditable());
            box.getChildren().add(picker);
            box.setAlignment(Pos.CENTER);
            HBox.setHgrow(picker, Priority.ALWAYS);
        } else {
            var ar = createArrayDisplay(n, state);
            box.getChildren().add(ar);
            HBox.setHgrow(ar, Priority.ALWAYS);
        }
        return box;
    }

    private static Region createArrayDisplay(EditorNode n, EditorState state) {
        var box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(5);

        {
            int length = n.getRawSize();
            var btn = new Button("List (" + length + ")");
            var icon = new FontIcon();
            GuiTooltips.install(icon, "Expand node");
            btn.getStyleClass().add("list-expand-button");
            btn.setGraphic(icon);
            btn.setAlignment(Pos.CENTER);
            btn.setOnAction(e -> state.getNavigation().navigateToChild(n));
            box.getChildren().add(btn);
        }

        {
            var preview = new Label();
            preview.getStyleClass().add("preview");
            preview.setGraphic(new FontIcon());
            preview.setOnMouseEntered(e -> {
                var text = NodeWriter.writeToString(
                        n.toWritableNode(),
                        AppPrefs.get().editorMaxTooltipLines().getValue(),
                        AppPrefs.get().editorIndentation().getValue().getValue());
                var tt = TooltipHelper.create(new ReadOnlyStringWrapper(text), null);
                tt.setWrapText(false);
                tt.setShowDelay(Duration.ZERO);
                Tooltip.install(preview, tt);
            });
            box.getChildren().add(preview);
        }

        return box;
    }
}
