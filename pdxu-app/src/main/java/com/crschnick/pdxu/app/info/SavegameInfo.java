package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import com.jfoenix.controls.JFXMasonryPane;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public abstract class SavegameInfo<T> {

    private static final ThreadLocal<Consumer<Throwable>> COMP_ERROR_COLLECTOR = new ThreadLocal<>();

    public static void collectCompErrors(Consumer<Throwable> collector, Runnable r) {
        COMP_ERROR_COLLECTOR.set(collector);
        try {
            r.run();
        } finally {
            COMP_ERROR_COLLECTOR.remove();
        }
    }

    private record SimulatedPlayer(Object tag, List<?> allTags) {}

    private static final ThreadLocal<SimulatedPlayer> SIMULATED_PLAYER = new ThreadLocal<>();

    public static void simulatePlayer(Object tag, List<?> allTags, Runnable r) {
        SIMULATED_PLAYER.set(new SimulatedPlayer(tag, allTags));
        try {
            r.run();
        } finally {
            SIMULATED_PLAYER.remove();
        }
    }

    private static void handleCompError(Throwable t) {
        var collector = COMP_ERROR_COLLECTOR.get();
        if (collector != null) {
            collector.accept(t);
        } else {
            ErrorEventFactory.fromThrowable(t).handle();
        }
    }

    protected SavegameData<T> data;

    protected SavegameInfo() {}

    protected SavegameInfo(SavegameContent content) throws Exception {
        this.data = getDataClass().getDeclaredConstructor().newInstance();

        try {
            var simulated = SIMULATED_PLAYER.get();
            if (simulated != null) {
                @SuppressWarnings("unchecked")
                var tags = (List<T>) simulated.allTags();
                @SuppressWarnings("unchecked")
                var tag = (T) simulated.tag();
                this.data.init(content, tag, tags);
            } else {
                this.data.init(content);
            }
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }

        for (var field : getClass().getDeclaredFields()) {
            if (!SavegameContentReader.class.isAssignableFrom(field.getType())) {
                continue;
            }

            try {
                var c = (SavegameContentReader)
                        field.getType().getDeclaredConstructors()[0].newInstance();
                if (c.requiresPlayer() && data.getTag() == null) {
                    continue;
                }

                c.init(content, data);
                field.setAccessible(true);
                field.set(this, c);
            } catch (InvocationTargetException e) {
                handleCompError(e.getCause());
            } catch (Exception e) {
                handleCompError(e);
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
                    handleCompError(ex);
                }
            }

            if (SavegameInfoMultiComp.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    SavegameInfoMultiComp c = (SavegameInfoMultiComp) field.get(this);
                    if (c == null) {
                        continue;
                    }

                    var created = c.create(data);
                    if (created == null) {
                        continue;
                    }

                    comps.addAll(created);
                } catch (Exception ex) {
                    handleCompError(ex);
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
                handleCompError(ex);
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
        content.minWidthProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> p.getWidth()
                                - p.getPadding().getLeft()
                                - p.getPadding().getRight(),
                        p.widthProperty()));
        content.prefHeightProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> p.getHeight()
                                - p.getPadding().getTop()
                                - p.getPadding().getBottom(),
                        p.heightProperty()));
    }

    private JFXMasonryPane createEmptyContainer() {
        JFXMasonryPane container = new JFXMasonryPane();
        container.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER);
        container.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);
        container.setHSpacing(10);
        container.setVSpacing(10);
        container
                .minHeightProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> 3 * container.getCellHeight()
                                + 2 * container.getVSpacing()
                                + container.getPadding().getBottom()
                                + container.getPadding().getTop(),
                        container.paddingProperty()));
        container.setLimitRow(3);
        container.getStyleClass().add(getStyleClass());
        return container;
    }
}
