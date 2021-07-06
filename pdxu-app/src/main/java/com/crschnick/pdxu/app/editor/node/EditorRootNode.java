package com.crschnick.pdxu.app.editor.node;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;

import java.util.List;

public class EditorRootNode extends EditorRealNode {

    private ArrayNode root;

    public EditorRootNode(String keyName, int parentIndex, ArrayNode root) {
        super(null, keyName, parentIndex);
        this.root = root;
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
