package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.util.List;

public class StackComp extends Comp<CompStructure<StackPane>> {

    private final List<Comp<?>> comps;

    public StackComp(List<Comp<?>> comps) {
        this.comps = List.copyOf(comps);
    }

    @Override
    public CompStructure<StackPane> createBase() {
        var pane = new StackPane();
        for (var c : comps) {
            pane.getChildren().add(c.createRegion());
        }
        pane.setAlignment(Pos.CENTER);
        return new SimpleCompStructure<>(pane);
    }
}
