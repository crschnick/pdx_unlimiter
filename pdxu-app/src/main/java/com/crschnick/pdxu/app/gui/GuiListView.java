package com.crschnick.pdxu.app.gui;

import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.function.Function;

public class GuiListView {

    @SuppressWarnings("unchecked")
    public static <T> Region createViewOfList(
            ListProperty<T> list,
            Function<T, Node> nodeFactory,
            boolean fixSize
    ) {
        Pane pane = new Pane();

        Platform.runLater(() -> {
            JFXListView<Node> listView = new JFXListView<>();
            var newItems = list.stream()
                    .map(li -> createForItem(li, nodeFactory)).toList();

            listView.prefWidthProperty().bind(pane.widthProperty());
            listView.prefHeightProperty().bind(pane.heightProperty());

            pane.getChildren().setAll(listView);

            if (fixSize && newItems.size() > 0) {
                listView.fixedCellSizeProperty().bind(((Region) newItems.get(0)).heightProperty());
            }
            newItems.forEach(li -> listView.getItems().add(li));
            listView.setExpanded(true);
        });

        list.addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var map = new HashMap<T, Node>();
                ((JFXListView<Node>) pane.getChildren().get(0))
                        .getItems().forEach(node -> map.put((T) node.getProperties().get("list-item"), node));

                JFXListView<Node> listView = new JFXListView<>();
                var newItems = n.stream()
                        .map(li -> {
                            var def = map.get(li);
                            if (def == null) {
                                def = createForItem(li, nodeFactory);
                            }
                            return def;
                        }).toList();

                map.clear();

                listView.prefWidthProperty().bind(pane.widthProperty());
                listView.prefHeightProperty().bind(pane.heightProperty());

                var old = (JFXListView<?>) pane.getChildren().get(0);
                old.getItems().clear();
                pane.getChildren().setAll(listView);

                if (fixSize && newItems.size() > 0) {
                    listView.fixedCellSizeProperty().bind(((Region) newItems.get(0)).heightProperty());
                }
                newItems.forEach(li -> listView.getItems().add(li));
                listView.setExpanded(true);
            });
        });
        return pane;
    }

    private static <T> Node createForItem(T li, Function<T, Node> nodeFactory) {
        var node = nodeFactory.apply(li);
        node.getProperties().put("list-item", li);
        return node;
    }
}
