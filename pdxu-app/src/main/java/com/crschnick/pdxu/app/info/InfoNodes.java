package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.GuiTooltips;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;
import static com.crschnick.pdxu.app.gui.game.GameImage.imageNode;

public class InfoNodes {

    public record ValueEntry(String text, Image icon, String tooltip) {

        public ValueEntry(String text, Image icon) {
            this(text, icon, null);
        }
    }

    public static Region createValueRow(List<ValueEntry> entries) {
        var box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(8);
        for (var entry : entries) {
            var label = new Label(entry.text(), imageNode(entry.icon(), CLASS_POWER_ICON));
            label.setEllipsisString("");
            label.setMinWidth(Region.USE_PREF_SIZE);
            if (entry.tooltip() != null) {
                GuiTooltips.install(label, entry.tooltip());
            }
            box.getChildren().add(label);
        }
        return box;
    }

    public static Region createRulerNode(Image icon, String name, String tooltip, Region stats) {
        var label = new Label(name);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var header = new HBox(imageNode(icon, CLASS_RULER_ICON), label);
        header.setAlignment(Pos.CENTER);
        header.setSpacing(5);

        var box = new VBox(header, stats);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add(CLASS_RULER);
        GuiTooltips.install(box, tooltip);
        return box;
    }
}
