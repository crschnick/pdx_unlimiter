package com.crschnick.pdxu.editor.node;

import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;

import java.util.List;
import java.util.Objects;

public class EditorRootNode extends EditorRealNode {

    private final EditorState state;
    private ArrayNode root;

    public EditorRootNode(String keyName, int parentIndex, EditorState state, ArrayNode root) {
        super(null, keyName, parentIndex);
        this.state = state;
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EditorRootNode that = (EditorRootNode) o;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), state);
    }

    @Override
    public void updateNodeAtRawIndex(Node replacementValue, String toInsertKeyName, int index) {
        var replacement = toInsertKeyName != null ?
                ArrayNode.singleKeyNode(toInsertKeyName, replacementValue) : ArrayNode.array(List.of(replacementValue));
        root = root.replacePart(replacement, index, 1);
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public void update(ArrayNode newNode) {
        this.root = newNode;
    }

    @Override
    public Node getNodeAtRawIndex(int index) {
        return root.getNodeArray().get(index);
    }

    @Override
    public Node getBackingNode() {
        return root;
    }
}
