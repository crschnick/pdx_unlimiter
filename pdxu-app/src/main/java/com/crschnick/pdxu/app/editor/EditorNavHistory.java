package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.NodePointer;
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
    private final IntegerProperty historyPos;

    public EditorNavHistory(EditorState state) {
        var init = EditorNavPath.empty();
        this.history = new ArrayList<>();
        this.current = new SimpleObjectProperty<>(init);
        this.history.add(current.get());
        this.state = state;
        this.historyPos = new SimpleIntegerProperty(0);
    }

    public BooleanBinding canGoBackProperty() {
        return Bindings.createBooleanBinding(() -> historyPos.get() > 0, historyPos);
    }

    public BooleanBinding canGoForwardProperty() {
        return Bindings.createBooleanBinding(() -> historyPos.get() < history.size() - 1, historyPos);
    }

    public void goBack() {
        if (historyPos.get() == 0) {
            return;
        }

        historyPos.set(historyPos.get() - 1);
        var goTo = history.get(historyPos.get());
        this.current.set(goTo);
        state.getContent().navigate(goTo.getEditorNode(), goTo.getPage(), goTo.getScroll());
    }

    public void goForward() {
        if (historyPos.get() == history.size()) {
            return;
        }

        historyPos.set(historyPos.get() + 1);
        var goTo = history.get(historyPos.get());
        this.current.set(goTo);
        state.getContent().navigate(goTo.getEditorNode(), goTo.getPage(), goTo.getScroll());
    }

    private void removeHistoryAfterPos() {
        if (history.size() > historyPos.get() + 1) {
            history.subList(historyPos.get() + 1, history.size()).clear();
        }
    }

    public void replaceCurrentNavPath(EditorNavPath p) {
        this.current.set(p);
        this.state.getContent().navigate(p.getEditorNode(), p.getPage(), p.getScroll());
        this.history.set(historyPos.get(), p);
    }

    public void navigateTo(EditorNavPath p) {
        if (EditorNavPath.areNodePathsEqual(current.get(), p)) {
            return;
        }

        removeHistoryAfterPos();

        this.current.set(p);
        this.state.getContent().navigate(p.getEditorNode(), p.getPage(), p.getScroll());

        this.history.add(p);
        historyPos.set(historyPos.get() + 1);
    }


    public void navigateTo(NodePointer pointer) {
        EditorNavPath.createNavPath(state, pointer).ifPresent(n -> {
            navigateTo(n);
        });
    }

    public void navigateTo(EditorNode newNode) {
        var newPath = EditorNavPath.navigateTo(current.get(), newNode);
        navigateTo(newPath);
    }

    public EditorNavPath getCurrent() {
        return current.get();
    }

    public ObjectProperty<EditorNavPath> currentProperty() {
        return current;
    }
}