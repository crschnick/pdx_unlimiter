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

    private final Map<Comp<?>, ObservableValue<Boolean>> content;

    public MultiContentComp(Map<Comp<?>, ObservableValue<Boolean>> content) {
        this.content = FXCollections.observableMap(content);
    }

    @Override
    protected Region createSimple() {
        ObservableMap<Comp<?>, Region> m = FXCollections.observableHashMap();
        var stack = new StackPane();
        m.addListener((MapChangeListener<? super Comp<?>, Region>) change -> {
            if (change.wasAdded()) {
                stack.getChildren().add(change.getValueAdded());
            } else {
                stack.getChildren().remove(change.getValueRemoved());
            }
        });

        for (Map.Entry<Comp<?>, ObservableValue<Boolean>> e : content.entrySet()) {
            var r = e.getKey().createRegion();
            e.getValue().subscribe(val -> {
                PlatformThread.runLaterIfNeeded(() -> {
                    r.setManaged(val);
                    r.setVisible(val);
                });
            });
            m.put(e.getKey(), r);
        }

        return stack;
    }
}
