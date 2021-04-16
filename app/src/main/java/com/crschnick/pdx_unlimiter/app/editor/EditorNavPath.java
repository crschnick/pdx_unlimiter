package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.core.node.NodePointer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditorNavPath {

    public static class NavEntry {
        private final EditorNode editorNode;
        private final IntegerProperty page;
        private final DoubleProperty scroll;

        NavEntry(EditorNode editorNode, double scroll) {
            this.editorNode = editorNode;
            this.page = new SimpleIntegerProperty(0);
            this.scroll = new SimpleDoubleProperty(scroll);
        }

        public EditorNode getEditorNode() {
            return editorNode;
        }

        public double getScroll() {
            return scroll.get();
        }

        public DoubleProperty scrollProperty() {
            return scroll;
        }
    }

    private List<NavEntry> path;

    private EditorNavPath(List<NavEntry> path) {
        this.path = path;
    }

    public static Optional<EditorNavPath> createNavPath(EditorState state, NodePointer pointer) {
        EditorNode current = null;
        List<NavEntry> newPath = new ArrayList<>();

        for (var e : state.getRootNodes().values()) {
            var found = pointer.sub(0, 1).isValid(e.getContent());
            if (found) {
                current = e;
                newPath.add(new NavEntry(e, 0));
                break;
            }
        }
        if (newPath.size() == 0) {
            return Optional.empty();
        }

        for (int i = 0; i < pointer.size(); i++) {
            int ci = i;
            var exp = current.expand();
            var found = exp.stream()
                    .filter(en -> pointer.sub(ci, ci +1).isValid(en.getContent()))
                    .findFirst();
            if (found.isEmpty()) {
                return Optional.empty();
            } else {
                //TODO: Fix scroll
                newPath.add(new NavEntry(found.get(), 0));
                current = found.get();
            }
        }

        return Optional.of(new EditorNavPath(newPath));
    }

    public List<NavEntry> getPath() {
        return path;
    }
}
