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
        var b = getBackingNode();
        if (!b.isValue()) {
            return;
        }

        ValueNode bn = b.getValueNode();

        // Don't let length go to 0
        if (text.length() == 0 && !bn.isQuoted()) {
            return;
        }

        bn.set(new ValueNode(text, bn.isQuoted()));
    }

    public void updateColor(Color c) {
        var b = getBackingNode();
        if (!b.isTagged()) {
            return;
        }

        TaggedNode cn = b.getTaggedNode();
        var newColorNode = ColorHelper.toColorNode(c);
        cn.set(newColorNode);
    }

    public void updateNodeAtRawIndex(Node replacementValue, String toInsertKeyName, int index) {
        var b = getBackingNode();
        if (!b.isArray()) {
            return;
        }

        ArrayNode ar = b.getArrayNode();
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
            return;
        }

        if (newNode.getNodeArray().size() == 0) {
            throw new IllegalArgumentException("Can't assign empty value to node. Delete the key and value in the parent node instead.");
        }

        if (newNode.getNodeArray().size() == 1) {
            var nodeToUse = newNode.getNodeArray().getFirst();
            // Update parent node to reflect change
            getParent().updateNodeAtRawIndex(nodeToUse, keyName, getRawIndexInParentNode());
            return;
        }

        throw new IllegalArgumentException("Unable to assign multiple values to a singular value node. If you want to convert this node to an array, edit the parent node instead.");
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
