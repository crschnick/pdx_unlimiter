package com.crschnick.pdx_unlimiter.app.gui;

import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GuiListView {

    public static <T> ListView<Node> createViewOfList(
            ListProperty<T> list,
            Function<T, Node> nodeFactory) {
        JFXListView<Node> listView = new JFXListView<Node>();

        Platform.runLater(() -> {
            var newItems = list.stream()
                    .map(li -> createForItem(li, nodeFactory))
                    .collect(Collectors.toList());
            listView.getItems().setAll(newItems);
        });

        list.addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var map = new HashMap<T, Node>();
                listView.getItems().forEach(node -> map.put((T) node.getProperties().get("list-item"), node));

                var newItems = n.stream()
                        .map(li -> map.getOrDefault(li, createForItem(li, nodeFactory)))
                        .collect(Collectors.toList());
                listView.getItems().setAll(newItems);

                // Bug in JFoenix? We have to set this everytime we update the list view
                listView.setExpanded(true);
            });
        });
        return listView;
    }

    private static <T> Node createForItem(T li, Function<T, Node> nodeFactory) {
        var node = nodeFactory.apply(li);
        node.getProperties().put("list-item", li);
        return node;
    }
}
