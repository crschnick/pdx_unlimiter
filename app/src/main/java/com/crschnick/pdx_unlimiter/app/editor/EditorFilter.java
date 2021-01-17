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
    private ObjectProperty<Scope> scope;
    EditorFilter(EditorState state) {
        this.state = state;

        filterString = new SimpleStringProperty("");
        filterString.addListener((c, o, n) -> state.update());
        deep = new SimpleBooleanProperty();
        deep.addListener((c, o, n) -> state.update());
        caseSensitive = new SimpleBooleanProperty();
        caseSensitive.addListener((c, o, n) -> state.update());
        scope = new SimpleObjectProperty<>(Scope.KEY);
        scope.addListener((c, o, n) -> state.update());
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
            if (true) {
                if ((scope.get() == Scope.KEY || scope.get() == Scope.BOTH) && contains("")) {
                    return true;
                } else if ((scope.get() == Scope.VALUE || scope.get() == Scope.BOTH)) {
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        }).collect(Collectors.toList());
    }

    public String getFilterString() {
        return filterString.get();
    }

    public StringProperty filterStringProperty() {
        return filterString;
    }

    public boolean isDeep() {
        return deep.get();
    }

    public BooleanProperty deepProperty() {
        return deep;
    }

    public boolean isCaseSensitive() {
        return caseSensitive.get();
    }

    public BooleanProperty caseSensitiveProperty() {
        return caseSensitive;
    }

    public Scope getScope() {
        return scope.get();
    }

    public ObjectProperty<Scope> scopeProperty() {
        return scope;
    }

    public enum Scope {
        KEY,
        VALUE,
        BOTH
    }
}
