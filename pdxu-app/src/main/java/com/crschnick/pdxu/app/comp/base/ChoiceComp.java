package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.platform.PlatformThread;
import com.crschnick.pdxu.app.util.Translatable;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ChoiceComp<T> extends Comp<CompStructure<ComboBox<T>>> {

    Property<T> value;
    ObservableValue<Map<T, ObservableValue<String>>> range;
    boolean includeNone;

    public ChoiceComp(Property<T> value, Map<T, ObservableValue<String>> range, boolean includeNone) {
        this.value = value;
        this.range = new SimpleObjectProperty<>(range);
        this.includeNone = includeNone;
    }

    public static <T extends Translatable> ChoiceComp<T> ofTranslatable(
            Property<T> value, List<T> range, boolean includeNone) {
        var map = range.stream()
                .collect(
                        Collectors.toMap(o -> o, Translatable::toTranslatedString, (v1, v2) -> v2, LinkedHashMap::new));
        return new ChoiceComp<>(value, map, includeNone);
    }

    @Override
    public CompStructure<ComboBox<T>> createBase() {
        var cb = new ComboBox<T>();
        cb.setConverter(new StringConverter<>() {
            @Override
            public String toString(T object) {
                if (object == null) {
                    return AppI18n.get("none");
                }

                var found = range.getValue().get(object);
                if (found == null) {
                    return "";
                }

                return found.getValue();
            }

            @Override
            public T fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });
        range.subscribe(c -> {
            var list = FXCollections.observableArrayList(c.keySet());
            if (!list.contains(null) && includeNone) {
                list.add(null);
            }

            cb.getItems().setAll(list);
        });

        cb.valueProperty().addListener((observable, oldValue, newValue) -> {
            value.setValue(newValue);
        });
        value.subscribe(val -> {
            PlatformThread.runLaterIfNeeded(() -> cb.valueProperty().set(val));
        });

        cb.getStyleClass().add("choice-comp");
        cb.setMaxWidth(10000);
        return new SimpleCompStructure<>(cb);
    }
}
