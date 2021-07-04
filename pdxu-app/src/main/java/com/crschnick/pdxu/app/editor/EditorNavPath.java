package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.io.node.NodePointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditorNavPath {

    public static boolean areNodePathsEqual(EditorNavPath p1, EditorNavPath p2) {
        if (p1.getPath().size() != p2.getPath().size()) {
            return false;
        }

        // Start from 1, since editor node at 0 is always null
        for (int i = 1; i < p1.getPath().size(); i++) {
            if (!p1.getPath().get(i).getNavigationName().equals(
                    p2.getPath().get(i).getNavigationName())) {
                return false;
            }
        }

        return true;
    }

    private final List<EditorNode> path;
    private final int page;
    private final double scroll;

    public EditorNavPath(List<EditorNode> path, int page, double scroll) {
        this.path = path;
        this.page = page;
        this.scroll = scroll;
    }

    public int getPage() {
        return page;
    }

    public double getScroll() {
        return scroll;
    }

    public EditorNode getEditorNode() {
        return path.get(path.size() - 1);
    }

    public static EditorNavPath pathInFocus(List<EditorNode> path, EditorNode focus) {
        if (path.size() == 1) {
            return empty();
        }

        if (path.size() == 2) {
            return new EditorNavPath(path, 0, 0.0);
        }

        var nodeSize = path.get(path.size() - 2).getSize();
        var pageSizes = EditorContent.calculatePageSizes(nodeSize);
        var last = path.get(path.size() - 1);
        int sum = 0;
        for (int page = 0; page < pageSizes.size(); page++) {
            if (sum + pageSizes.get(page) > last.getParentIndex()) {
                double scroll = (double) (last.getParentIndex() - sum) / pageSizes.get(page);
                return new EditorNavPath(path, page, scroll);
            }

            sum += pageSizes.get(page);
        }
        return new EditorNavPath(path, pageSizes.size() -1, 1.0);
    }

    public static EditorNavPath empty() {
        var list = new ArrayList<EditorNode>();
        list.add(null);
        return new EditorNavPath(list, 0, 0.0);
    }

    public static EditorNavPath navigateTo(EditorNavPath path, EditorNode node) {
        if (node == null) {
            return empty();
        } else {
            int index = path.getPath().indexOf(node);
            if (index == -1) {
                var newList = new ArrayList<>(path.getPath());
                newList.add(node);
                return new EditorNavPath(newList, 0, 0.0);
            } else {
                return fromPath(path.getPath().subList(0, index + 1));
            }
        }
    }

    private static Optional<EditorNode> fastEditorNodeFind(EditorNode current, NodePointer sub) {
        if (current.isReal() && sub.getPath().get(0).name() != null) {
            EditorSimpleNode s = (EditorSimpleNode) current;
            return EditorNode.fastEditorSimpleNodeSearch(
                    current, s.getBackingNode().getArrayNode(), sub.getPath().get(0).name());
        }

        var exp = current.expand();
        for (var en : exp) {
            if (sub.isValid(en.getContent())) {
                return Optional.of(en);
            }
        }
        return Optional.empty();
    }

    public static Optional<EditorNavPath> createNavPath(EditorState state, NodePointer pointer) {
        EditorNode current = null;
        List<EditorNode> newPath = new ArrayList<>();
        newPath.add(null);

        for (var e : state.getRootNodes().values()) {
            var found = pointer.sub(0, 1).isValid((e).getBackingNode());
            if (found) {
                current = e;
                newPath.add(e);
                break;
            }
        }
        // Not found
        if (current == null) {
            return Optional.empty();
        }

        for (int i = 0; i < pointer.size(); i++) {
            var sub = pointer.sub(i, i +1);
            var found = fastEditorNodeFind(current, sub);
            if (found.isEmpty()) {
                return Optional.empty();
            } else {
                newPath.add(found.get());
                current = found.get();
            }
        }

        return Optional.of(fromPath(newPath));
    }


    public static EditorNavPath verify(EditorNavPath input) {
        EditorNode current = null;
        List<EditorNode> newPath = new ArrayList<>();
        for (var navEl : input.getPath()) {
            if (current == null) {
                current = navEl;
                newPath.add(current);
                continue;
            }

            var newEditorNode = current.expand().stream()
                    .filter(en -> navEl.getParentIndex() == en.getParentIndex() &&
                            en.getDisplayKeyName().equals(navEl.getDisplayKeyName()))
                    .findFirst();
            if (newEditorNode.isPresent()) {
                current = newEditorNode.get();
                newPath.add(newEditorNode.get());
            } else {
                break;
            }
        }
        return fromPath(newPath);
    }

    public List<EditorNode> getPath() {
        return path;
    }
}