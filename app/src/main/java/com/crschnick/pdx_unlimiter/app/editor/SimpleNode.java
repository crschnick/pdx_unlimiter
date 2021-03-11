package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.node.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.function.Predicate;

public final class SimpleNode extends EditorNode {

    private final int keyIndex;
    private Node backingNode;

    public SimpleNode(EditorNode directParent, String keyName, int parentIndex, int keyIndex, Node backingNode) {
        super(directParent, keyName, parentIndex);
        this.keyIndex = keyIndex;
        this.backingNode = backingNode;
    }

    public void updateText(String text) {
        ValueNode bn = (ValueNode) backingNode;
        var val = bn.isQuoted() ? "\"" + text + "\"" : text;
        bn.set(new ValueNode(val));
    }

    public void updateColor(Color c) {
        ColorNode cn = (ColorNode) backingNode;
        var newColorNode = ColorHelper.toColorNode(c);
        cn.set(newColorNode);
    }

    public void replacePart(ArrayNode toInsert, int beginIndex, int length) {
        ArrayNode ar = (ArrayNode) backingNode;
        this.backingNode = ar.replacePart(toInsert, beginIndex, length);

        // Update parent node to reflect change
        if (getDirectParent() != null) {
            getRealParent().replacePart((ArrayNode) this.backingNode, getKeyIndex(), 1);
        }
    }

    @Override
    public void delete() {
        if (getDirectParent() != null) {
            getRealParent().replacePart(ArrayNode.emptyArray(), getKeyIndex(), 1);
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
        return this.backingNode.matches(matcher);
    }

    @Override
    public String getDisplayKeyName() {
        return getKeyName().orElse("[" + keyIndex + "]");
    }

    @Override
    public String getNavigationName() {
        return getKeyName().orElseGet(() -> getDirectParent().getNavigationName() + "[" + keyIndex + "]");
    }

    @Override
    public boolean isReal() {
        return true;
    }

    @Override
    public SimpleNode getRealParent() {
        return getDirectParent().isReal() ? (SimpleNode) getDirectParent() : getDirectParent().getRealParent();
    }

    @Override
    public List<EditorNode> expand() {
        return EditorNode.create(this, (ArrayNode) backingNode);
    }

    public ArrayNode toWritableNode() {
        return backingNode.isArray() ? (ArrayNode) backingNode :
                ArrayNode.array(List.of(backingNode));
    }

    public void update(ArrayNode newNode) {
        if (backingNode.isArray()) {
            this.backingNode = newNode;

            // Update parent node to reflect change
            if (getDirectParent() != null) {
                var replacement = getKeyName().map(k -> ArrayNode.singleKeyNode(k, this.backingNode))
                        .orElse(ArrayNode.array(this.backingNode.getNodeArray()));
                getRealParent().replacePart(replacement, getKeyIndex(), 1);
            }
        } else {
            var nodeToUse = newNode.getNodeArray().get(0);
            if (nodeToUse.isColor()) {
                ((ColorNode) backingNode).set((ColorNode) nodeToUse);
            } else if (nodeToUse.isValue()) {
                ((ValueNode) backingNode).set((ValueNode) nodeToUse);
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public Node getBackingNode() {
        return backingNode;
    }
}
