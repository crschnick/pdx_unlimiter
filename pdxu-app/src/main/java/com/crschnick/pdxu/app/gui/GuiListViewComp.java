package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.function.Function;

@AllArgsConstructor
public class GuiListViewComp<T> extends SimpleComp {

    private  final ListProperty<T> list;
    private  final Function<T, Comp<?>> nodeFactory;
    private  final boolean fixSize;

    private Region createForItem(T li, Function<T, Comp<?>> nodeFactory) {
        var node = nodeFactory.apply(li).createRegion();
        node.getProperties().put("list-item", li);
        return node;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Region createSimple() {
        Pane pane = new Pane();

        Platform.runLater(() -> {
            ListView<Node> listView = new ListView<>();
            var newItems = list.stream()
                    .map(li -> createForItem(li, nodeFactory)).toList();

            listView.prefWidthProperty().bind(pane.widthProperty());
            listView.prefHeightProperty().bind(pane.heightProperty());

            pane.getChildren().setAll(listView);

            if (fixSize && newItems.size() > 0) {
                listView.fixedCellSizeProperty().bind(((Region) newItems.getFirst()).heightProperty());
            }
            newItems.forEach(li -> listView.getItems().add(li));
        });

        list.addListener((c, o, n) -> {
            Platform.runLater(() -> {
                var map = new HashMap<T, Region>();
                ((ListView<Node>) pane.getChildren().getFirst())
                        .getItems().forEach(node -> map.put((T) node.getProperties().get("list-item"), (Region) node));

                ListView<Node> listView = new ListView<>();
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

                var old = (ListView<?>) pane.getChildren().getFirst();
                old.getItems().clear();
                pane.getChildren().setAll(listView);

                if (fixSize && newItems.size() > 0) {
                    listView.fixedCellSizeProperty().bind(((Region) newItems.getFirst()).heightProperty());
                }
                newItems.forEach(li -> listView.getItems().add(li));
            });
        });
        return pane;
    }
}
