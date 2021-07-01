package com.crschnick.pdxu.app.editor;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class EditorNavHistory {

    private final EditorState state;
    private final List<EditorNavPath> history;
    private final IntegerProperty position;

    public EditorNavHistory(EditorState state) {
        this.history = new ArrayList<>();
        this.history.add(state.getNavPath());
        this.state = state;
        this.position = new SimpleIntegerProperty(0);
    }

    public BooleanBinding canGoBackProperty() {
        return Bindings.createBooleanBinding(() -> position.get() > 0, position);
    }

    public BooleanBinding canGoForwardProperty() {
        return Bindings.createBooleanBinding(() -> position.get() < history.size() - 1, position);
    }

    public void goBack() {
        if (position.get() == 0) {
            return;
        }

        position.set(position.get() - 1);
        state.navigateTo(history.get(position.get()));
    }


    public void goForward() {
        if (position.get() == history.size() - 1) {
            return;
        }

        position.set(position.get() + 1);
        state.navigateTo(history.get(position.get()));
    }

    public void changeNavPath(EditorNavPath p) {
        if (history.get(position.get()).equals(p)) {
            return;
        }

        var old = state.getNavPath();
        this.history.add(old);
        position.set(position.get() + 1);
    }
}