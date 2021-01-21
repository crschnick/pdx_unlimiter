package com.crschnick.pdx_unlimiter.app.editor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.List;
import java.util.stream.Collectors;

public class EditorFilter {

    private StringProperty filterString;
    private BooleanProperty caseSensitive;
    private BooleanProperty filterKeys;
    private BooleanProperty filterValues;

    EditorFilter(EditorState state) {
        filterString = new SimpleStringProperty("");
        filterString.addListener((c, o, n) -> {
            if (filterKeys.get() || filterValues.get()) {
                state.update(false);
            }
        });
        caseSensitive = new SimpleBooleanProperty();
        caseSensitive.addListener((c, o, n) -> state.update(false));
        filterKeys = new SimpleBooleanProperty(true);
        filterKeys.addListener((c, o, n) -> state.update(false));
        filterValues = new SimpleBooleanProperty(true);
        filterValues.addListener((c, o, n) -> state.update(false));
    }

    private boolean contains(String s) {
        if (caseSensitive.get()) {
            return s.contains(filterString.get());
        } else {
            return s.toLowerCase().contains(filterString.get().toLowerCase());
        }
    }

    public List<EditorNode> filter(List<EditorNode> input) {
        return input.stream().filter(n -> {
            if (!filterKeys.get() && !filterValues.get()) {
                return true;
            }

            if (filterString.get().length() == 0) {
                return true;
            }

            if (filterKeys.get() && n.filterKey(this::contains)) {
                return true;
            } else if (filterValues.get() && n.filterValue(this::contains)) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    public StringProperty filterStringProperty() {
        return filterString;
    }

    public BooleanProperty caseSensitiveProperty() {
        return caseSensitive;
    }

    public boolean isFilterKeys() {
        return filterKeys.get();
    }

    public BooleanProperty filterKeysProperty() {
        return filterKeys;
    }

    public boolean isFilterValues() {
        return filterValues.get();
    }

    public BooleanProperty filterValuesProperty() {
        return filterValues;
    }
}
