package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public abstract class SavegameInfo<T> {

    protected SavegameData<T> data;

    protected SavegameInfo() {
    }

    protected SavegameInfo(SavegameContent content) throws Exception {
        this.data = getDataClass().getDeclaredConstructor().newInstance();
        this.data.init(content);

        for (var field : getClass().getDeclaredFields()) {
            if (!SavegameInfoComp.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                var c = (SavegameInfoComp) field.getType().getDeclaredConstructors()[0].newInstance();
                c.init(content, data);
                field.setAccessible(true);
                field.set(this, c);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public SavegameData<T> getData() {
        return data;
    }

    protected abstract String getStyleClass();

    protected abstract Class<? extends SavegameData<T>> getDataClass();

    public final Region createContainer() {
        var container = createEmptyContainer();

        for (var field : getClass().getDeclaredFields()) {
            if (!SavegameInfoComp.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                field.setAccessible(true);
                SavegameInfoComp c = (SavegameInfoComp) field.get(this);
                if (c == null) {
                    continue;
                }
                var region = c.create(data);
                if (region != null) {
                    addNode(container, region);
                }
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

    private JFXMasonryPane createEmptyContainer() {
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
        container.getStyleClass().add(getStyleClass());
        return container;
    }
}
