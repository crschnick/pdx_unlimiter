package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.io.node.Node;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class GuiEditorTypes {

    private static Region createTypeNode(Color c, char ch, Insets insets, String tooltip) {
        Circle circle = new Circle(10);
        circle.setFill(c);
        Label label = new Label(String.valueOf(ch));
        label.setPadding(insets);
        GuiTooltips.install(label, tooltip);
        var sp = new StackPane(circle, label);
        StackPane.setAlignment(label, Pos.CENTER);
        return sp;
    }

    public static Region createTypeNode(EditorNode n) {
        if (n.isReal()) {
            var desc = ((EditorRealNode) n).getBackingNode().describe();
            return GuiEditorTypes.createTypeNode(desc);
        } else {
            return createCollectorTypeNode();
        }
    }

    private static Region createCollectorTypeNode() {
        return createTypeNode(
                new Color(0.8, 0.3, 0.9, 0.8),
                'S',
                new Insets(0, 0, 1, 1),
                "Synthetic collection");
    }

    private static Region createTypeNode(Node.Descriptor desc) {
        if (desc.getValueType() == null ||
                desc.getKeyType().equals(Node.KeyType.MIXED) ||
                desc.getKeyType().equals(Node.KeyType.ALL)) {
            return createTypeNode(
                    new Color(0.2, 0.2, 0.2, 0.3),
                    'C',
                    new Insets(0, 2, 1, 0),
                    "Complex type");
        }

        if (desc.getValueType() == Node.ValueType.BOOLEAN) {
            return createTypeNode(
                    new Color(0.2, 0.2, 1, 0.5),
                    'B',
                    new Insets(0, 0, 1, 1),
                    "Boolean");
        }
        if (desc.getValueType() == Node.ValueType.TEXT) {
            return createTypeNode(
                    new Color(0.2, 1, 0.2, 0.5),
                    'T',
                    new Insets(1, 0, 0, 1),
                    "Text");
        }
        if (desc.getValueType() == Node.ValueType.INTEGER) {
            return createTypeNode(
                    new Color(1, 0.2, 0.2, 0.5),
                    'I',
                    new Insets(0, 0, 1, 1),
                    "Integer");
        }

        if (desc.getValueType() == Node.ValueType.FLOATING_POINT) {
            return createTypeNode(
                    new Color(0.4, 1, 1, 0.5),
                    'F',
                    new Insets(1, 0.3, 0, 1),
                    "Floating point number");
        }

        if (desc.getValueType() == Node.ValueType.UNQUOTED_STRING) {
            return createTypeNode(
                    new Color(1, 0.8, 0.3, 0.5),
                    'V',
                    new Insets(1, 0.3, 0, 1),
                    "Game specific value");
        }
        if (desc.getValueType() == Node.ValueType.COLOR) {
            return createTypeNode(
                    new Color(0.9, 0.3, 0.9, 0.3),
                    'C',
                    new Insets(0, 2, 1, 0),
                    "Color");
        }

        throw new IllegalStateException();
    }
}
