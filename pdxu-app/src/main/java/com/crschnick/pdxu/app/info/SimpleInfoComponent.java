package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.function.Function;
import java.util.function.Predicate;


public class SimpleInfoComponent implements SavegameInfoComponent {

    public static SimpleInfoComponent create(Image icon, Color color, String tooltip, Predicate<ArrayNode> func) {

    }

    public static SimpleInfoComponent create(Image icon, Color color, String tooltip, Function<ArrayNode, String> func) {

    }

    private Image icon;
    private Color color;
    private String tooltip;
    private String value;

    private SimpleInfoComponent(Image icon, Color color, String tooltip, String value) {
        this.icon = icon;
        this.color = color;
        this.tooltip = tooltip;
        this.value = value;
    }

    @Override
    public Region create() {
        return null;
    }
}
