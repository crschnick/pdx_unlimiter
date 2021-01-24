package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.core.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatWriter;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.jfoenix.controls.JFXButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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

    private static Region createTypeNode(ValueNode.Type type) {
        if (type == ValueNode.Type.BOOLEAN) {
            return createTypeNode(
                    new Color(0.2, 0.2, 1, 0.5),
                    'B',
                    new Insets(0, 0, 1, 1),
                    "Boolean");
        }
        if (type == ValueNode.Type.TEXT) {
            return createTypeNode(
                    new Color(0.2, 1, 0.2, 0.5),
                    'T',
                    new Insets(0, 0.3, 0, 0),
                    "Text");
        }
        if (type == ValueNode.Type.INTEGER) {
            return createTypeNode(
                    new Color(1, 0.2, 0.2, 0.5),
                    'I',
                    new Insets(0, 0, 1, 1),
                    "Integer");
        }

        if (type == ValueNode.Type.FLOATING_POINT) {
            return createTypeNode(
                    new Color(0.4, 1, 1, 0.5),
                    'F',
                    new Insets(0, 0.3, 0, 0),
                    "Floating point number");
        }

        if (type == ValueNode.Type.GAME_VALUE) {
            return createTypeNode(
                    new Color(1, 0.8, 0.3, 0.5),
                    'V',
                    new Insets(0, 0.3, 0, 1),
                    "Game specific value");
        }
        return null;
    }


    static Optional<Region> createTypeNode(EditorNode n) {
        boolean isSimple = n.isReal() && ((SimpleNode) n).getBackingNode() instanceof ValueNode;
        if (isSimple) {
            return Optional.ofNullable(createTypeNode(((ValueNode) ((SimpleNode) n).getBackingNode()).determineType()));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<ValueNode.Type> determineListType(ArrayNode an) {
        if (an.getNodes().size() == 0) {
            return Optional.empty();
        }

        if (an.getNodes().size() == 1) {
            return an.getNodes().get(0) instanceof ValueNode ?
                    Optional.ofNullable(((ValueNode) an.getNodes().get(0)).determineType()) : Optional.empty();
        }

        Optional<ValueNode.Type> t0 = an.getNodes().get(0) instanceof ValueNode ?
                Optional.ofNullable(((ValueNode) an.getNodes().get(0)).determineType()) : Optional.empty();
        Optional<ValueNode.Type> t1 = an.getNodes().get(1) instanceof ValueNode ?
                Optional.ofNullable(((ValueNode) an.getNodes().get(1)).determineType()) : Optional.empty();
        if (t0.equals(t1)) {
            return t0;
        } else {
            return Optional.empty();
        }
    }

    static Region createValueDisplay(EditorNode n, EditorState state) {
        boolean isText = n.isReal() && ((SimpleNode) n).getBackingNode() instanceof ValueNode;
        if (isText) {
            var tf = new TextField(((SimpleNode) n).getBackingNode().getString());
            tf.setAlignment(Pos.CENTER);
            tf.textProperty().addListener((c, o, ne) -> {
                ((SimpleNode) n).updateText(ne);
                state.onTextChanged();
            });
            return tf;
        } else {
            var box = new HBox();
            box.setAlignment(Pos.CENTER);
            box.setSpacing(5);

            {
                Optional<ValueNode.Type> type = n.isReal() ? determineListType(
                        (ArrayNode) ((SimpleNode) n).getBackingNode()) : Optional.empty();
                type.ifPresentOrElse(t -> {
                    box.getChildren().add(createTypeNode(t));
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
                    var tt = TextFormatWriter.writeToString(n.toWritableNode(), 15, "  ");
                    GuiTooltips.install(preview, tt);
                });
                box.getChildren().add(preview);
            }

            return box;
        }
    }
}
