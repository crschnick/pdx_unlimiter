package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.platform.PlatformThread;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Map;

public class MultiContentComp extends SimpleComp {

    private final boolean lazy;
    private final Map<Comp<?>, ObservableValue<Boolean>> content;

    public MultiContentComp(Map<Comp<?>, ObservableValue<Boolean>> content) {
        this(true, content);
    }

    public MultiContentComp(boolean lazy, Map<Comp<?>, ObservableValue<Boolean>> content) {
        this.lazy = lazy;
        this.content = FXCollections.observableMap(content);
    }

    @Override
    protected Region createSimple() {
        ObservableMap<Comp<?>, StackPane> m = FXCollections.observableHashMap();
        var stack = new StackPane();
        m.addListener((MapChangeListener<? super Comp<?>, StackPane>) change -> {
            if (change.wasAdded()) {
                stack.getChildren().add(change.getValueAdded());
            } else {
                stack.getChildren().remove(change.getValueRemoved());
            }
        });

        for (Map.Entry<Comp<?>, ObservableValue<Boolean>> e : content.entrySet()) {
            // Only build the content once it is shown for the first time to speed up the startup
            var r = new StackPane();
            if (!lazy) {
                r.getChildren().add(e.getKey().createRegion());
            }
            e.getValue().subscribe(val -> {
                PlatformThread.runLaterIfNeeded(() -> {
                    if (val && r.getChildren().isEmpty()) {
                        r.getChildren().add(e.getKey().createRegion());
                    }

                    r.setManaged(val);
                    r.setVisible(val);
                });
            });
            m.put(e.getKey(), r);
        }

        return stack;
    }
}
