package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.editor.node.EditorRootNode;
import com.crschnick.pdxu.io.node.ArrayNode;
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

    public EditorNavPath(List<EditorNode> path) {
        this.path = path;
    }

    public EditorNode getEditorNode() {
        return path.get(path.size() - 1);
    }

    public static EditorNavPath empty() {
        var list = new ArrayList<EditorNode>();
        list.add(null);
        return new EditorNavPath(list);
    }

    public static EditorNavPath parentPath(EditorNavPath path, EditorNode node) {
        if (node == null) {
            return empty();
        } else {
            int index = path.getPath().indexOf(node);
            if (index == -1) {
                throw new IllegalArgumentException("Node not found in path");
            } else {
                return new EditorNavPath(path.getPath().subList(0, index + 1));
            }
        }
    }

    private static Optional<EditorNode> fastEditorNodeFind(ArrayNode content, EditorNode current, NodePointer sub) {
        if (current.isReal()) {
            EditorRealNode s = (EditorRealNode) current;
            var key = sub.getPath().get(0).getKey(content, s.getBackingNode());
            if (key != null) {
                return EditorNode.fastEditorSimpleNodeSearch(
                        current, s.getBackingNode().getArrayNode(), key);
            } else {
                var exp = current.expand();
                for (var en : exp) {
                    if (sub.isValid(en.getContent())) {
                        return Optional.of(en);
                    }
                }
                return Optional.empty();
            }
        } else {
            var expEdNodes =  current.expand();
            for (var en : expEdNodes) {
                var found = fastEditorNodeFind(content, en, sub);
                if (found.isPresent()) {
                    return found;
                }
            }
            return Optional.empty();
        }
    }

    public static Optional<EditorNavPath> createNavPath(EditorState state, NodePointer pointer) {
        if (!pointer.isValid(state.getBackingNode())) {
            return Optional.empty();
        }

        EditorRootNode root = null;
        List<EditorNode> newPath = new ArrayList<>();
        newPath.add(null);

        for (var e : state.getRootNodes().values()) {
            var found = pointer.sub(0, 1).isValid((e).getBackingNode());
            if (found) {
                root = e;
                newPath.add(e);
                break;
            }
        }
        // Not found
        if (root == null) {
            return Optional.empty();
        }

        EditorNode current = root;
        for (int i = 0; i < pointer.size(); i++) {
            var sub = pointer.sub(i, i +1);
            var found = fastEditorNodeFind(state.getBackingNode(), current, sub);
            if (found.isEmpty()) {
                return Optional.empty();
            } else {
                newPath.add(found.get());
                current = found.get();
            }
        }

        return Optional.of(new EditorNavPath(newPath));
    }

    public static EditorNavPath rebuild(EditorNavPath input) {
        List<EditorNode> newPath = new ArrayList<>();
        for (var navEl : input.getPath()) {
            if (navEl == null) {
                newPath.add(null);
                continue;
            }

            if (navEl.isValid()) {
                newPath.add(navEl);
            } else {
                break;
            }
        }
        return new EditorNavPath(newPath);
    }

    public List<EditorNode> getPath() {
        return path;
    }
}