package com.crschnick.pdx_unlimiter.app.gui;

import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;

import java.util.HashMap;
import java.util.function.Function;

public class GuiListView {

    public static <T> ListView<Node> createViewOfList(
            ObservableList<T> list,
            Function<T, Node> nodeFactory,
            ReadOnlyProperty<T> selectedProperty) {
        JFXListView<Node> listView = new JFXListView<Node>();
        list.addListener((ListChangeListener<? super T>) lc -> {
            Platform.runLater(() -> {
                var map = new HashMap<T, Node>();
                listView.getItems().forEach(n -> map.put((T) n.getProperties().get("list-item"), n));

                listView.getItems().clear();
                list.forEach(li -> {
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
