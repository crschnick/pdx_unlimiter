package com.crschnick.pdxu.app.info;


import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public abstract class SavegameInfo<T> {

    protected SavegameData<T> data;

    protected SavegameInfo() {
    }

    protected SavegameInfo(SavegameContent content) throws Exception {
        this.data = getDataClass().getDeclaredConstructor().newInstance();

        try {
            this.data.init(content);
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }

        for (var field : getClass().getDeclaredFields()) {
            if (!SavegameContentReader.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                var c = (SavegameContentReader) field.getType().getDeclaredConstructors()[0].newInstance();
                if (c.requiresPlayer() && data.getTag() == null) {
                    continue;
                }

                c.init(content, data);
                field.setAccessible(true);
                field.set(this, c);
            } catch (InvocationTargetException e) {
                ErrorEventFactory.fromThrowable(e.getCause()).handle();
            } catch (Exception e) {
                ErrorEventFactory.fromThrowable(e).handle();
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

        var comps = new ArrayList<SavegameInfoComp>();
        for (var field : getClass().getDeclaredFields()) {
            if (SavegameInfoComp.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    SavegameInfoComp c = (SavegameInfoComp) field.get(this);
                    if (c == null) {
                        continue;
                    }

                    if (c.requiresPlayer() && data.getTag() == null) {
                        continue;
                    }

                    comps.add(c);
                } catch (Exception ex) {
                    ErrorEventFactory.fromThrowable(ex).handle();
                }
            }

            if (SavegameInfoMultiComp.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    SavegameInfoMultiComp c = (SavegameInfoMultiComp) field.get(this);
                    if (c == null) {
                        continue;
                    }

                    comps.addAll(c.create(data));
                } catch (Exception ex) {
                    ErrorEventFactory.fromThrowable(ex).handle();
                }
            }
        }

        for (SavegameInfoComp comp : comps) {
            try {
                var region = comp.create(data);
                if (region != null) {
                    addNode(container, region);
                }
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).handle();
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
