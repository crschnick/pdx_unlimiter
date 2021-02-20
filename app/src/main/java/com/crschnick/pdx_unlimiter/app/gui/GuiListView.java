package com.crschnick.pdx_unlimiter.app.gui;

import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.function.Function;

public class GuiListView {

    public static <T> ListView<Node> createViewOfList(
            ListProperty<T> list,
            Function<T, Node> nodeFactory,
            ReadOnlyProperty<T> selectedProperty) {
        JFXListView<Node> listView = new JFXListView<Node>();
        list.addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var map = new HashMap<T, Node>();
                listView.getItems().forEach(node -> map.put((T) node.getProperties().get("list-item"), node));

                listView.getItems().clear();

                n.forEach(li -> {
                    var item = map.getOrDefault(li, createForItem(li, nodeFactory));
                    listView.getItems().add(item);
                });

                // Bug in JFoenix? We have to set this everytime we update the list view
                listView.setExpanded(true);
            });
        });
        selectedProperty.addListener((c, o, ne) -> {
            Platform.runLater(() -> {
                listView.getSelectionModel().clearSelection();
                if (ne != null) {
                    var map = new HashMap<T, Node>();
                    listView.getItems().forEach(n -> map.put((T) n.getProperties().get("list-item"), n));
                    listView.getSelectionModel().select(map.get(ne));
                }
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
