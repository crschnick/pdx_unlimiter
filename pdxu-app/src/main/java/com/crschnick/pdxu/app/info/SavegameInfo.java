package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.lang.reflect.InvocationTargetException;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public abstract class SavegameInfo<T extends SavegameData> {

    protected T data;

    protected SavegameInfo(ArrayNode node) throws Exception {
        try {
            this.data = (T) getDataClass().getDeclaredConstructors()[0].newInstance(node);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
            ErrorHandler.handleTerminalException(e);
        }

        for (var field : getClass().getFields()) {
            if (!SavegameInfoComp.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                field.set(this, field.getType().getDeclaredConstructors()[0].newInstance());
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                ErrorHandler.handleTerminalException(e);
            }

            SavegameInfoComp c = (SavegameInfoComp) field.get(this);
            c.init(node, this.data);
        }
    }

    protected abstract Class<T> getDataClass();

    public final Region createContainer() {
        var container = createEmptyContainer();

        for (var field : getClass().getFields()) {
            if (!SavegameInfoComp.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                SavegameInfoComp c = (SavegameInfoComp) field.get(this);
                var region = c.create();
                addNode(container, region);
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }

        return container;
    }

    private static void addNode(JFXMasonryPane pane, Region content) {
        content.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTENT);
        StackPane p = new StackPane(content);
        p.setAlignment(Pos.CENTER);
        p.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE);
        content.setPadding(new Insets(5, 10, 5, 10));
        p.setPrefWidth(Region.USE_COMPUTED_SIZE);
        pane.getChildren().add(p);

        // Magic! Using any other properties breaks the layout
        content.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> p.getWidth() - p.getPadding().getLeft() - p.getPadding().getRight(), p.widthProperty()));
        content.prefHeightProperty().bind(Bindings.createDoubleBinding(
                () -> p.getHeight() - p.getPadding().getTop() - p.getPadding().getBottom(), p.heightProperty()));
    }

    private static JFXMasonryPane createEmptyContainer() {
        JFXMasonryPane container = new JFXMasonryPane();
        container.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER);
        container.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);
        container.setHSpacing(10);
        container.setVSpacing(10);
        container.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> 3 * container.getCellHeight() +
                        2 * container.getVSpacing() +
                        container.getPadding().getBottom() +
                        container.getPadding().getTop(), container.paddingProperty()));
        container.setLimitRow(3);
        return container;
    }
}
