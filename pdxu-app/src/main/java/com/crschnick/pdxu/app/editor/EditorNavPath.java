package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.NodePointer;

import java.util.*;
import java.util.stream.Collectors;

public class EditorNavPath {

    public static boolean areNodePathsEqual(EditorNavPath p1, EditorNavPath p2) {
        if (p1.getPath().size() != p2.getPath().size()) {
            return false;
        }

        for (int i = 0; i < p1.getPath().size(); i++) {
            if (!p1.getPath().get(i).getEditorNode().getNavigationName().equals(
                    p2.getPath().get(i).getEditorNode().getNavigationName())) {
                break;
            }
        }

        return true;
    }

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

    private final List<NavEntry> path;

    public EditorNavPath(List<NavEntry> path) {
        this.path = path;
        if (path.size() == 0) {
            throw new IllegalArgumentException();
        }
    }

    public static EditorNavPath empty() {
        return new EditorNavPath(List.of(new NavEntry(null, 0, 0.0)));
    }

    public static EditorNavPath navigateTo(EditorNavPath path, EditorNode node) {
        if (node == null) {
            return empty();
        } else {
            int index = path.getPath().stream()
                    .map(EditorNavPath.NavEntry::getEditorNode)
                    .collect(Collectors.toList())
                    .indexOf(node);
            if (index == -1) {
                var newList = new ArrayList<>(path.getPath());
                newList.add(new EditorNavPath.NavEntry(node, 0, 0.0));
                return new EditorNavPath(newList);
            } else {
                return new EditorNavPath(path.getPath().subList(0, index + 1));
            }
        }
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
                newPath.add(new NavEntry(found.get(), 0, 0.0));
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
                newPath.add(new EditorNavPath.NavEntry(current, 0, 0.0));
                continue;
            }

            var newEditorNode = current.expand().stream()
                    .filter(en -> navEl.getEditorNode().getParentIndex() == en.getParentIndex() &&
                            en.getDisplayKeyName().equals(navEl.getEditorNode().getDisplayKeyName()))
                    .findFirst();
            if (newEditorNode.isPresent()) {
                current = newEditorNode.get();
                newPath.add(new EditorNavPath.NavEntry(newEditorNode.get(), navEl.getPage(), navEl.getScroll()));
            } else {
                break;
            }
        }
        return new EditorNavPath(newPath);
    }

    public NavEntry getLast() {
        return path.get(path.size() - 1);
    }

    public List<NavEntry> getPath() {
        return Collections.unmodifiableList(path);
    }
}