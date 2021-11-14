package com.crschnick.pdxu.editor.node;

import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.TaggedNode;
import com.crschnick.pdxu.io.node.ValueNode;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;

public final class EditorSimpleNode extends EditorRealNode {

    private final int rawIndexInParentNode;

    public EditorSimpleNode(EditorNode directParent, String keyName, int parentIndex, int rawIndexInParentNode) {
        super(directParent, keyName, parentIndex);
        this.rawIndexInParentNode = rawIndexInParentNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EditorSimpleNode that = (EditorSimpleNode) o;
        return rawIndexInParentNode == that.rawIndexInParentNode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rawIndexInParentNode);
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

    public void updateNodeAtRawIndex(Node replacementValue, String toInsertKeyName, int index) {
        ArrayNode ar = (ArrayNode) getBackingNode();

        var replacement = toInsertKeyName != null ?
                ArrayNode.singleKeyNode(toInsertKeyName, replacementValue) : ArrayNode.array(List.of(replacementValue));
        var updatedNode = ar.replacePart(replacement, index, 1);
        getParent().updateNodeAtRawIndex(updatedNode, keyName, getRawIndexInParentNode());
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    public void update(ArrayNode newNode) {
        if (getBackingNode().isArray()) {
            // Update parent node to reflect change
            getParent().updateNodeAtRawIndex(newNode, keyName, getRawIndexInParentNode());
        } else {
            if (newNode.getNodeArray().size() != 1) {
                throw new IllegalArgumentException("Can't assign array with size != 1 to value node");
            }

            var nodeToUse = newNode.getNodeArray().get(0);
            // Update parent node to reflect change
            getParent().updateNodeAtRawIndex(nodeToUse, keyName, getRawIndexInParentNode());
        }
    }

    public boolean isValid() {
        if (!getParent().isValid()) {
            return false;
        }

        if (getParent().getRawSize() <= rawIndexInParentNode) {
            return false;
        }

        return getParent().getNavigationNameAtRawIndex(rawIndexInParentNode).equals(getNavigationName());
    }


    @Override
    public Node getNodeAtRawIndex(int index) {
        return getBackingNode().getNodeArray().get(index);
    }

    public int getRawIndexInParentNode() {
        return rawIndexInParentNode;
    }

    public Node getBackingNode() {
        return getParent().getNodeAtRawIndex(rawIndexInParentNode);
    }
}
