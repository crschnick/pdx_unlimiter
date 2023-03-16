package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TagRows {

    private static final int WRAP_COUNT = 7;
    private static final int MAX_DISPLAY_COUNT = 14;

    public static <T> Region createTagRow(
            Region img,
            String tooltip,
            List<T> tags,
            Function<T, String> tooltipGen,
            Function<T, Region> gen) {
        GuiTooltips.install(img, tooltip);

        var list = tags.subList(0, Math.min(tags.size(), MAX_DISPLAY_COUNT)).stream()
                .filter(Objects::nonNull)
                .map(t -> {
                    var node = gen.apply(t);
                    GuiTooltips.install(node, tooltipGen.apply(t));
                    return node;
                })
                .collect(Collectors.toList());
        if (tags.size() <= WRAP_COUNT) {
            var box = new HBox();
            box.getChildren().add(img);
            box.getChildren().addAll(list);
            box.setFillHeight(true);
            box.setAlignment(Pos.CENTER);
            var vbox = new VBox(box);
            vbox.setFillWidth(true);
            vbox.setAlignment(Pos.CENTER);
            return vbox;
        }

        boolean exceeded = false;
        int displayedSize = list.size();
        if (tags.size() > MAX_DISPLAY_COUNT) {
            exceeded = true;
        }

        boolean even = displayedSize % 2 == 0;
        int firstRow = displayedSize / 2;
        int secondRow = displayedSize / 2 + (even ? 0 : 1);

        var first = new HBox();
        first.setAlignment(Pos.CENTER);
        first.getChildren().add(img);
        first.getChildren().addAll(list.subList(0, firstRow));

        var second = new HBox();
        second.setAlignment(Pos.CENTER);
        var spacer = new Region();
        spacer.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> (list.get(0).getMinWidth() / 2) - (first.getSpacing()),
                list.get(0).minWidthProperty(), first.spacingProperty()));
        second.getChildren().add(spacer);
        second.getChildren().addAll(list.subList(firstRow, firstRow + secondRow));
        if (exceeded) {
            String tt = tags.subList(firstRow + secondRow,
                    tags.size()).stream().map(tooltipGen).collect(Collectors.joining(", "));
            var label = new Label("...");
            GuiTooltips.install(label, tt);
            second.getChildren().add(label);
        }

        var rows = new VBox(first, second);
        rows.setFillWidth(true);
        rows.setAlignment(Pos.CENTER);
        return rows;
    }
}
