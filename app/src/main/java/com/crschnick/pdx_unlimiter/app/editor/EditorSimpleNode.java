package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.node.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Predicate;

public final class EditorSimpleNode extends EditorNode {

    private final int keyIndex;
    private final ObjectProperty<Node> backingNode;

    public EditorSimpleNode(EditorNode directParent, String keyName, int parentIndex, int keyIndex, Node backingNode) {
        super(directParent, keyName, parentIndex);
        this.keyIndex = keyIndex;
        this.backingNode = new SimpleObjectProperty<>(backingNode);
    }

    public void updateText(String text) {
        ValueNode bn = (ValueNode) getBackingNode();
        bn.set(new ValueNode(text, bn.isQuoted()));
    }

    public void updateColor(Color c) {
        ColorNode cn = (ColorNode) getBackingNode();
        var newColorNode = ColorHelper.toColorNode(c);
        cn.set(newColorNode);
    }

    public void updateNodeAtIndex(Node replacementValue, String toInsertKeyName, int index) {
        ArrayNode ar = (ArrayNode) getBackingNode();

        var replacement = toInsertKeyName != null ?
                ArrayNode.singleKeyNode(toInsertKeyName, replacementValue) : ArrayNode.array(List.of(replacementValue));
        this.backingNode.set(ar.replacePart(replacement, index, 1));
        if (getDirectParent() != null) {
            getDirectParent().updateNodeAtIndex(getBackingNode(), keyName, getKeyIndex());
        }
    }

    public void replacePart(ArrayNode toInsert, int beginIndex, int length) {
        ArrayNode ar = (ArrayNode) getBackingNode();
        this.backingNode.set(ar.replacePart(toInsert, beginIndex, length));

        // Update parent node to reflect change
        if (getDirectParent() != null) {
            getDirectParent().updateNodeAtIndex(getBackingNode(), keyName, getKeyIndex());
        }
    }

    @Override
    public void delete() {
        if (getDirectParent() != null) {
            getDirectParent().replacePart(ArrayNode.emptyArray(), getKeyIndex(), 1);
        }
    }

    @Override
    public boolean filterKey(Predicate<String> filter) {
        if (getKeyName().isPresent() && filter.test(getKeyName().get())) {
            return true;
        }

        return filter.test(String.valueOf(keyIndex));
    }

    @Override
    public boolean filterValue(NodeMatcher matcher) {
        return getBackingNode().matches(matcher);
    }

    @Override
    public String getDisplayKeyName() {
        return getKeyName().orElse("[" + keyIndex + "]");
    }

    @Override
    public String getNavigationName() {
        return getKeyName().orElseGet(() -> "[" + keyIndex + "]");
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public EditorSimpleNode getRealParent() {
        return getDirectParent().isReal() ? (EditorSimpleNode) getDirectParent() : getDirectParent().getRealParent();
    }

    @Override
    public List<EditorNode> expand() {
        return EditorNode.create(this, (ArrayNode) getBackingNode());
    }

    public ArrayNode toWritableNode() {
        return getBackingNode().isArray() ? (ArrayNode) getBackingNode() :
                ArrayNode.array(List.of(getBackingNode()));
    }

    public void update(ArrayNode newNode) {
        if (getBackingNode().isArray()) {
            this.backingNode.set(newNode);

            // Update parent node to reflect change
            if (getDirectParent() != null) {
                getDirectParent().updateNodeAtIndex(getBackingNode(), keyName, getKeyIndex());
            }
        } else {
            if (newNode.getNodeArray().size() != 1) {
                throw new IllegalArgumentException("Can't assign array with size != 1 to value node");
            }

            var nodeToUse = newNode.getNodeArray().get(0);
            if (nodeToUse.isColor()) {
                ((ColorNode) getBackingNode()).set((ColorNode) nodeToUse);
            } else if (nodeToUse.isValue()) {
                ((ValueNode) getBackingNode()).set((ValueNode) nodeToUse);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public Node getBackingNode() {
        return backingNode.get();
    }

    public ObjectProperty<Node> backingNodeProperty() {
        return backingNode;
    }
}
