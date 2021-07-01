package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.NodePointer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EditorNavPath {

    public static class NavEntry {
        private final EditorNode editorNode;
        private int page;
        private double scroll;

        public NavEntry(EditorNode editorNode, int page, double scroll) {
            this.editorNode = editorNode;
            this.page = page;
            this.scroll = scroll;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public void setScroll(double scroll) {
            this.scroll = scroll;
        }

        public int getPage() {
            return page;
        }

        public double getScroll() {
            return scroll;
        }

        public EditorNode getEditorNode() {
            return editorNode;
        }
    }

    private List<NavEntry> path;

    public EditorNavPath(List<NavEntry> path) {
        this.path = path;
    }

    public static Optional<EditorNavPath> createNavPath(Collection<EditorNode> rootNodes, NodePointer pointer) {
        EditorNode current = null;
        List<NavEntry> newPath = new ArrayList<>();

        for (var e : rootNodes) {
            var found = pointer.sub(0, 1).isValid(e.getContent());
            if (found) {
                current = e;
                newPath.add(new NavEntry(e, 0, 0.0));
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


    public static EditorNavPath verify(EditorNavPath input, Collection<EditorNode> rootNodes) {
        EditorNode current = null;
        List<EditorNavPath.NavEntry> newPath = new ArrayList<>();
        for (var navEl : input.getPath()) {
            if (current == null) {
                current = navEl.getEditorNode();
                newPath.add(new EditorNavPath.NavEntry(current, 0));
                continue;
            }

            var newEditorNode = current.expand().stream()
                    .filter(en -> navEl.getEditorNode().getParentIndex() == en.getParentIndex() &&
                            en.getDisplayKeyName().equals(navEl.getEditorNode().getDisplayKeyName()))
                    .findFirst();
            if (newEditorNode.isPresent()) {
                current = newEditorNode.get();
                newPath.add(new EditorNavPath.NavEntry(newEditorNode.get(), navEl.getScroll()));
            } else {
                break;
            }
        }
        return new EditorNavPath(newPath);
    }

    public List<NavEntry> getPath() {
        return path;
    }
}