package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.editor.node.EditorRootNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodePointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EditorNavPath {

    private final List<EditorNode> path;

    public EditorNavPath(List<EditorNode> path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EditorNavPath that = (EditorNavPath) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public EditorNode getEditorNode() {
        return path.getLast();
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
            var key = sub.getPath().getFirst().getKey(content, s.getBackingNode());
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

    public static EditorNavPath rebaseToValid(EditorNavPath input) {
        List<EditorNode> newPath = new ArrayList<>();
        newPath.add(null);
        for (int i = 1; i < input.getPath().size(); i++) {
            var current = input.getPath().get(i);
            if (!current.isValid()) {
                break;
            }

            newPath.add(current);
        }

        return new EditorNavPath(newPath);
    }

    public List<EditorNode> getPath() {
        return path;
    }
}