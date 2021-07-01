package com.crschnick.pdxu.app.editor;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class EditorNavHistory {

    private final ObjectProperty<EditorNavPath> current;
    private final EditorState state;
    private final List<EditorNavPath> history;
    private final IntegerProperty position;

    public EditorNavHistory(EditorState state) {
        this.history = new ArrayList<>();
        this.current = new SimpleObjectProperty<>(new EditorNavPath(List.of(
                new EditorNavPath.NavEntry(null, 0, 0.0))));
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

        state.navigateTo(history.get(position.get()));
        position.set(position.get() - 1);
    }

    public void goForward() {
        if (position.get() == history.size() - 1) {
            return;
        }

        position.set(position.get() + 1);
        state.navigateTo(history.get(position.get()));
    }

    private void removeHistoryAfterPos() {
        if (history.size() > position.get() + 1) {
            history.subList(position.get() + 1, history.size()).clear();
        }
    }

    public void changeNavPath(EditorNavPath p) {
        if (EditorNavPath.areNodePathsEqual(current.get(), p)) {
            return;
        }

        removeHistoryAfterPos();

        this.history.add(current.get());
        position.set(position.get() + 1);
        this.current.set(p);
    }

    public EditorNavPath getCurrent() {
        return current.get();
    }

    public ObjectProperty<EditorNavPath> currentProperty() {
        return current;
    }
}