package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.platform.BindingsHelper;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

import lombok.AllArgsConstructor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
public class GuiListViewComp<T> extends SimpleComp {

    private final ListProperty<T> list;
    private final Function<T, Comp<?>> nodeFactory;
    private final boolean fixSize;
    private final Supplier<Collection<T>> allItems;

    @Override
    protected Region createSimple() {
        Pane pane = new Pane();
        ListView<T> listView = new ListView<>();
        listView.prefWidthProperty().bind(pane.widthProperty());
        listView.prefHeightProperty().bind(pane.heightProperty());
        pane.getChildren().add(listView);

        // Nodes are created lazily once an item is first displayed.
        // Only accessed from the platform thread
        Map<T, Region> nodes = new HashMap<>();
        Function<T, Region> getNode =
                item -> nodes.computeIfAbsent(item, li -> nodeFactory.apply(li).createRegion());

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : getNode.apply(item));
            }
        });

        AppLayoutModel.get().getSelected().addListener((observable, oldValue, newValue) -> {
            listView.getSelectionModel().clearSelection();
        });

        Runnable updateFixedSize = () -> {
            if (!fixSize
                    || listView.getItems().isEmpty()
                    || listView.fixedCellSizeProperty().isBound()) {
                return;
            }

            // All cells have the same size, so binding once is enough.
            // Don't rebind later on as the new node might not be laid out yet and still have a height of 0
            listView.fixedCellSizeProperty()
                    .bind(getNode.apply(listView.getItems().getFirst()).heightProperty());
        };

        // Take a snapshot now, as the list might already have changed by the time the update runs.
        // Items might also have been removed in the meantime, so only show items that still exist
        Consumer<List<T>> update = newItems -> {
            var retained = new HashSet<>(allItems.get());
            nodes.keySet().removeIf(item -> !retained.contains(item));
            BindingsHelper.setContent(
                    listView.getItems(),
                    newItems.stream().filter(retained::contains).toList());
            updateFixedSize.run();
        };

        var initialItems = List.copyOf(list);
        Platform.runLater(() -> update.accept(initialItems));

        list.addListener((c, o, n) -> {
            var newItems = List.copyOf(n);
            Platform.runLater(() -> update.accept(newItems));
        });

        return pane;
    }
}
