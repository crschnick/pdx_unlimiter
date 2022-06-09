package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;

public class EditorNavigation {

    private final ObjectProperty<EditorNavLocation> current;
    private final EditorState state;
    private final List<EditorNavLocation> history;
    private final IntegerProperty historyPos;

    public EditorNavigation(EditorState state) {
        var init = new EditorNavLocation(EditorNavPath.empty(), 0, 0.0);
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
        restoreNavLocation(goTo);
    }

    public void goForward() {
        if (historyPos.get() == history.size()) {
            return;
        }

        historyPos.set(historyPos.get() + 1);
        var goTo = history.get(historyPos.get());
        restoreNavLocation(goTo);
    }

    private void restoreNavLocation(EditorNavLocation goTo) {
        var valid = EditorNavPath.rebaseToValid(goTo.path());
        if (!goTo.path().equals(valid)) {
            goTo = new EditorNavLocation(valid);
        }

        this.current.set(goTo);
        state.getContent().navigate(goTo.getEditorNode(), goTo.page(), goTo.scroll());
    }

    private void removeHistoryAfterPos() {
        if (history.size() > historyPos.get() + 1) {
            history.subList(historyPos.get() + 1, history.size()).clear();
        }
    }

    public boolean affectsCurrent(EditorNode changed) {
        return getCurrent().path().getPath().contains(changed) ||
                (changed.getParent() != null) && getCurrent().path().getPath().contains(changed.getParent());
    }

    public boolean rebaseNavPathsToValid() {
        var currentValid = EditorNavPath.rebaseToValid(this.current.get().path());
        var currentChanged = !currentValid.equals(current.get().path());
        var newLoc = !currentChanged ? current.get() :
                new EditorNavLocation(currentValid);
        this.current.set(newLoc);
        this.history.set(historyPos.get(), newLoc);

        // Rebase all history entries
        for (int i = 0; i < this.history.size(); i++) {
            var newNavPath = EditorNavPath.rebaseToValid(this.history.get(i).path());
            if (!newNavPath.equals(this.history.get(i).path())) {
                this.history.set(i, new EditorNavLocation(newNavPath));
            }
        }
        return currentChanged;
    }

    public void navigateTo(EditorNavPath p) {
        if (current.get().path().equals(p)) {
            return;
        }

        // Fix validity
        p = EditorNavPath.rebaseToValid(p);

        removeHistoryAfterPos();

        var newLoc = new EditorNavLocation(p, 0, 0.0);
        this.current.set(newLoc);
        this.state.getContent().navigate(p.getEditorNode(), newLoc.page(), newLoc.scroll());

        this.history.add(newLoc);
        historyPos.set(historyPos.get() + 1);
    }


    public boolean navigateTo(NodePointer pointer) {
        var np = EditorNavPath.createNavPath(state, pointer);
        np.ifPresent(this::navigateTo);
        return np.isPresent();
    }

    public void navigateToChild(EditorNode newNode) {
        var list = new ArrayList<>(current.get().path().getPath());
        list.add(newNode);
        navigateTo(new EditorNavPath(list));
    }

    public void navigateToParent(EditorNode newNode) {
        if (getCurrent().getEditorNode() == null) {
            return;
        }

        if (getCurrent().getEditorNode().equals(newNode)) {
            return;
        }

        removeHistoryAfterPos();

        var newPath = EditorNavPath.parentPath(getCurrent().path(), newNode);
        var inFocus = current.get().path().getPath().get(newPath.getPath().size());
        var newLoc = state.getContent().navigateAndFocus(newPath, inFocus);

        this.current.set(newLoc);
        this.history.add(newLoc);
        historyPos.set(historyPos.get() + 1);
    }

    public EditorNavLocation getCurrent() {
        return current.get();
    }

    public ObjectProperty<EditorNavLocation> currentProperty() {
        return current;
    }
}