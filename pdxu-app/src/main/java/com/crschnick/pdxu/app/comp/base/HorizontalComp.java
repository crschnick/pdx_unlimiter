package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import com.crschnick.pdxu.app.platform.PlatformThread;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.util.List;

public class HorizontalComp extends Comp<CompStructure<HBox>> {

    private final ObservableList<Comp<?>> entries;

    public HorizontalComp(List<Comp<?>> comps) {
        entries = FXCollections.observableArrayList(List.copyOf(comps));
    }

    public HorizontalComp(ObservableList<Comp<?>> entries) {
        this.entries = PlatformThread.sync(entries);
    }

    public Comp<CompStructure<HBox>> spacing(double spacing) {
        return apply(struc -> struc.get().setSpacing(spacing));
    }

    @Override
    public CompStructure<HBox> createBase() {
        var b = new HBox();
        b.getStyleClass().add("horizontal-comp");
        entries.addListener((ListChangeListener<? super Comp<?>>) c -> {
            b.getChildren().setAll(c.getList().stream().map(Comp::createRegion).toList());
        });
        for (var entry : entries) {
            b.getChildren().add(entry.createRegion());
        }
        b.setAlignment(Pos.CENTER);
        return new SimpleCompStructure<>(b);
    }
}
