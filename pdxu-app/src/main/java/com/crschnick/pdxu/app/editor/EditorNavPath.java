package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.NodePointer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EditorNavPath {

    public static int getViewIndex(NavEntry e, List<Integer> pageSizes) {
        if (pageSizes.size() == 0) {
            return 0;
        }

        if (e.getPage() >= pageSizes.size()) {
            return 0;
        }

        int index = 0;
        for (int i = 0; i < e.getPage(); i++) {
            index += pageSizes.get(i);
        }
        return index + (int) Math.floor(e.getScroll() * pageSizes.get(e.getPage()));
    }

    public static NavEntry createInView(EditorNode node, int index, List<Integer> pageSizes) {
        int sum = 0;
        for (int page = 0; page < pageSizes.size(); page++) {
            if (sum + pageSizes.get(page) > index) {
                double scroll = (double) (index - sum) / pageSizes.get(page);
                return new NavEntry(node, page, scroll);
            }

            sum += pageSizes.get(page);
        }
        return new NavEntry(node, pageSizes.size() -1, 1.0);
    }

    public static boolean areNodePathsEqual(EditorNavPath p1, EditorNavPath p2) {
        if (p1.getPath().size() != p2.getPath().size()) {
            return false;
        }

        // Start from 1, since editor node at 0 is always null
        for (int i = 1; i < p1.getPath().size(); i++) {
            if (!p1.getPath().get(i).getEditorNode().getNavigationName().equals(
                    p2.getPath().get(i).getEditorNode().getNavigationName())) {
                return false;
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
        if (path.get(0).getEditorNode() != null) {
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

    public static Optional<EditorNavPath> createNavPath(EditorState state, NodePointer pointer) {
        EditorNode current = null;
        List<NavEntry> newPath = new ArrayList<>();
        newPath.add(new NavEntry(null, 0, 0.0));

        for (var e : state.getRootNodes().values()) {
            var found = pointer.sub(0, 1).isValid(((EditorSimpleNode) e).getBackingNode());
            if (found) {
                current = e;
                newPath.add(new NavEntry(e, 0, 0.0));
                break;
            }
        }
        // Not found
        if (current == null) {
            return Optional.empty();
        }

        for (int i = 0; i < pointer.size(); i++) {
            int ci = i;
            var exp = current.expand();
            var foundIndex = IntStream.range(0, exp.size())
                    .filter(en -> pointer.sub(ci, ci +1).isValid(exp.get(en).getContent()))
                    .mapToObj(en -> (Integer) en)
                    .findFirst();
            var found = foundIndex.map(in -> exp.get(in));
            if (found.isEmpty()) {
                return Optional.empty();
            } else {
                // Update page and scroll of parent
                var replace = createInView(newPath.get(i + 1).getEditorNode(), foundIndex.get(), EditorContent.calculatePageSizes(exp));
                newPath.set(i + 1, replace);

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