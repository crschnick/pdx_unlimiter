package com.crschnick.pdx_unlimiter.app.gui.editor;

import com.crschnick.pdx_unlimiter.app.editor.CollectorNode;
import com.crschnick.pdx_unlimiter.app.editor.EditorNode;
import com.crschnick.pdx_unlimiter.app.editor.EditorState;
import com.crschnick.pdx_unlimiter.app.editor.SimpleNode;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.ColorNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;
import com.crschnick.pdx_unlimiter.core.parser.NodeWriter;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXColorPicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

public class GuiEditorNode {


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

    private static Region createComplexTypeNode() {
        return createTypeNode(
                new Color(0.2, 0.2, 0.2, 0.3),
                'C',
                new Insets(0, 2, 1, 0),
                "Complex type");
    }

    private static Region createTypeNode(Node.ValueType type) {
        if (type == Node.ValueType.BOOLEAN) {
            return createTypeNode(
                    new Color(0.2, 0.2, 1, 0.5),
                    'B',
                    new Insets(0, 0, 1, 1),
                    "Boolean");
        }
        if (type == Node.ValueType.TEXT) {
            return createTypeNode(
                    new Color(0.2, 1, 0.2, 0.5),
                    'T',
                    new Insets(1, 0, 0, 1),
                    "Text");
        }
        if (type == Node.ValueType.INTEGER) {
            return createTypeNode(
                    new Color(1, 0.2, 0.2, 0.5),
                    'I',
                    new Insets(0, 0, 1, 1),
                    "Integer");
        }

        if (type == Node.ValueType.FLOATING_POINT) {
            return createTypeNode(
                    new Color(0.4, 1, 1, 0.5),
                    'F',
                    new Insets(1, 0.3, 0, 1),
                    "Floating point number");
        }

        if (type == Node.ValueType.UNQUOTED_STRING) {
            return createTypeNode(
                    new Color(1, 0.8, 0.3, 0.5),
                    'V',
                    new Insets(1, 0.3, 0, 1),
                    "Game specific value");
        }
        if (type == Node.ValueType.COLOR) {
            return createTypeNode(
                    new Color(0.9, 0.3, 0.9, 0.3),
                    'C',
                    new Insets(0, 2, 1, 0),
                    "Color");
        }

        throw new IllegalStateException();
    }


    static Optional<Region> createTypeNode(EditorNode n) {
        if (n.isReal() && ((SimpleNode) n).getBackingNode().isColor()) {
            var d = ((SimpleNode) n).getBackingNode();
            return Optional.of(createTypeNode(d.describe().getValueType()));
        } else {
            return Optional.empty();
        }
    }

    static Region createValueDisplay(EditorNode n, EditorState state) {
        boolean isText = n.isReal() && ((SimpleNode) n).getBackingNode() instanceof ValueNode;
        boolean isColor = n.isReal() && ((SimpleNode) n).getBackingNode().isColor();
        if (isText) {
            var tf = new TextField(((SimpleNode) n).getBackingNode().getString());
            tf.setAlignment(Pos.CENTER);
            tf.textProperty().addListener((c, o, ne) -> {
                ((SimpleNode) n).updateText(ne);
                state.onTextChanged();
            });
            return tf;
        } else if (isColor) {
            var picker = new JFXColorPicker(ColorHelper.fromColorNode((ColorNode) ((SimpleNode) n).getBackingNode()));
            picker.valueProperty().addListener((c, o, ne) -> {
                ((SimpleNode) n).updateColor(ne);
                state.onColorChanged();
            });
            return picker;
        } else {
            var box = new HBox();
            box.setAlignment(Pos.CENTER);
            box.setSpacing(5);

            {
                var descriptor = Optional.ofNullable(
                        n.isReal() ? ((SimpleNode) n).getBackingNode().describe() : null);
                descriptor.ifPresentOrElse(t -> {
                    box.getChildren().add(createTypeNode(t.getValueType()));
                }, () -> {
                    box.getChildren().add(createComplexTypeNode());
                });
            }

            {
                int length = n.isReal() ? ((SimpleNode) n).getBackingNode().getNodeArray().size() :
                        ((CollectorNode) n).getNodes().size();
                int stringSize = String.valueOf(length).length();
                var lengthString = stringSize == 1 ? " " + length + " " :
                        (stringSize <= 3 ? " ".repeat(3 - stringSize) + String.valueOf(length) :
                                String.valueOf(length));
                var btn = new JFXButton("List[" + lengthString + "]");
                btn.setAlignment(Pos.CENTER);
                btn.setOnAction(e -> {
                    state.navigateTo(n);
                });
                box.getChildren().add(btn);
            }

            {
                var preview = new Label();
                preview.getStyleClass().add("preview");
                preview.setGraphic(new FontIcon());
                preview.setOnMouseEntered(e -> {
                    var tt = new Tooltip(NodeWriter.writeToString(n.toWritableNode(), 15, "  "));
                    tt.setShowDelay(Duration.ZERO);
                    Tooltip.install(preview, tt);
                });
                box.getChildren().add(preview);
            }

            return box;
        }
    }
}
