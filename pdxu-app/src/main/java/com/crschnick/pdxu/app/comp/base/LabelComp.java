package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.Listeners;
import com.crschnick.pdxu.app.platform.PlatformThread;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LabelComp extends Comp<CompStructure<Label>> {

    private final ObservableValue<String> text;
    private final ObservableValue<LabelGraphic> graphic;

    public LabelComp(String text, LabelGraphic graphic) {
        this(new SimpleStringProperty(text), new SimpleObjectProperty<>(graphic));
    }

    public LabelComp(String text) {
        this(new SimpleStringProperty(text));
    }

    public LabelComp(ObservableValue<String> text) {
        this(text, new SimpleObjectProperty<>());
    }

    @Override
    public CompStructure<Label> createBase() {
        var label = new Label();
        Listeners.subscribeWithScene(label, text, t -> {
            PlatformThread.runLaterIfNeeded(() -> label.setText(t));
        });
        Listeners.subscribeWithScene(label, graphic, t -> {
            PlatformThread.runLaterIfNeeded(() -> label.setGraphic(t != null ? t.createGraphicNode() : null));
        });
        label.setAlignment(Pos.CENTER);
        return new SimpleCompStructure<>(label);
    }
}
