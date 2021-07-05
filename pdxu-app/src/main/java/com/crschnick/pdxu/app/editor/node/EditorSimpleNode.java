package com.crschnick.pdxu.app.editor.node;

import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.TaggedNode;
import com.crschnick.pdxu.io.node.ValueNode;
import javafx.scene.paint.Color;

import java.util.List;

public final class EditorSimpleNode extends EditorRealNode {

    private final int keyIndex;

    public EditorSimpleNode(EditorNode directParent, String keyName, int parentIndex, int keyIndex) {
        super(directParent, keyName, parentIndex);
        this.keyIndex = keyIndex;
    }

    public void updateText(String text) {
        ValueNode bn = (ValueNode) getBackingNode();

        // Don't let length go to 0
        if (text.length() == 0 && !bn.isQuoted()) {
            return;
        }

        bn.set(new ValueNode(text, bn.isQuoted()));
    }

    public void updateColor(Color c) {
        TaggedNode cn = (TaggedNode) getBackingNode();
        var newColorNode = ColorHelper.toColorNode(c);
        cn.set(newColorNode);
    }

    public void updateNodeAtIndex(Node replacementValue, String toInsertKeyName, int index) {
        ArrayNode ar = (ArrayNode) getBackingNode();

        var replacement = toInsertKeyName != null ?
                ArrayNode.singleKeyNode(toInsertKeyName, replacementValue) : ArrayNode.array(List.of(replacementValue));
        var updatedNode = ar.replacePart(replacement, index, 1);
        getParent().updateNodeAtIndex(updatedNode, keyName, getKeyIndex());
    }

    public void replacePart(ArrayNode toInsert, int beginIndex, int length) {
        ArrayNode ar = (ArrayNode) getBackingNode();
        var updatedNode = ar.replacePart(toInsert, beginIndex, length);

        // Update parent node to reflect change
        getParent().updateNodeAtIndex(updatedNode, keyName, getKeyIndex());
    }

    @Override
    public void delete() {
        getParent().replacePart(ArrayNode.emptyArray(), getKeyIndex(), 1);
    }

    @Override
    public String getDisplayKeyName() {
        return getKeyName().orElse("[" + keyIndex + "]");
    }

    @Override
    public String getNavigationName() {
        return getKeyName().orElseGet(() -> getParent().getNavigationName() + "[" + keyIndex + "]");
    }

    public void update(ArrayNode newNode) {
        if (getBackingNode().isArray()) {
            // Update parent node to reflect change
            getParent().updateNodeAtIndex(this.getBackingNode(), keyName, getKeyIndex());
        } else {
            if (newNode.getNodeArray().size() != 1) {
                throw new IllegalArgumentException("Can't assign array with size != 1 to value node");
            }

            var nodeToUse = newNode.getNodeArray().get(0);
            if (nodeToUse.isTagged()) {
                ((TaggedNode) getBackingNode()).set((TaggedNode) nodeToUse);
            } else if (nodeToUse.isValue()) {
                ((ValueNode) getBackingNode()).set((ValueNode) nodeToUse);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public Node getNodeAtIndex(int index) {
        return getBackingNode().getNodeArray().get(index);
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public Node getBackingNode() {
        return getParent().getNodeAtIndex(getParentIndex());
    }
}
