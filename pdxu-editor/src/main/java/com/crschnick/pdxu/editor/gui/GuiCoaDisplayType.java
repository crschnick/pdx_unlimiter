package com.crschnick.pdxu.editor.gui;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.model.coa.CoatOfArms;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.util.Map;
import java.util.function.Consumer;

public abstract class GuiCoaDisplayType {

    protected static <T> Node createChoices(String nameKey, T defValue, Map<String, T> choices, Consumer<T> con) {
        var list = FXCollections.observableArrayList(
                choices.entrySet().stream().map(Map.Entry::getValue).toList());
        var cb = new ChoiceBox<>(list);
        cb.setValue(defValue);
        cb.valueProperty().addListener((c, o, n) -> {
            con.accept(n);
        });
        cb.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                return new DualHashBidiMap<>(choices).inverseBidiMap().get(object);
            }

            @Override
            public T fromString(String string) {
                return choices.get(string);
            }
        });

        var label = new Label();
        label.textProperty().bind(AppI18n.observable(nameKey).map(s -> s + ":"));
        HBox b = new HBox(label, cb);
        b.setAlignment(Pos.CENTER);
        b.setSpacing(6);
        return b;
    }

    protected final IntegerProperty size = new SimpleIntegerProperty();

    protected <T> void addChoice(
            GuiCoaViewerState<?> state, HBox box, String type, T defValue, Map<String, T> choices, Property<T> prop) {
        prop.setValue(defValue);
        box.getChildren().add(createChoices(type, defValue, choices, t -> {
            prop.setValue(t);
            state.updateImage();
        }));
    }

    public abstract Image render(CoatOfArms coa, GameFileContext ctx);

    public void addOptions(GuiCoaViewerState<?> state, HBox box) {}
}
