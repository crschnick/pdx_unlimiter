package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import javafx.beans.property.*;

import java.util.List;
import java.util.stream.Collectors;

public class EditorFilter {

    private EditorState state;
    private StringProperty filterString;
    private BooleanProperty deep;
    private BooleanProperty caseSensitive;
    private BooleanProperty filterKeys;
    private BooleanProperty filterValues;
    EditorFilter(EditorState state) {
        this.state = state;

        filterString = new SimpleStringProperty("");
        filterString.addListener((c, o, n) -> {
            if (filterKeys.get() || filterValues.get()) {
                state.update(false);
            }
        });
        deep = new SimpleBooleanProperty();
        deep.addListener((c, o, n) -> state.update(false));
        caseSensitive = new SimpleBooleanProperty();
        caseSensitive.addListener((c, o, n) -> state.update(false));
        filterKeys = new SimpleBooleanProperty(true);
        filterKeys.addListener((c, o, n) -> state.update(false));
        filterValues = new SimpleBooleanProperty(false);
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

    public BooleanProperty deepProperty() {
        return deep;
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
