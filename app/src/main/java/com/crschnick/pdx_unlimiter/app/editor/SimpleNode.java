package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.util.ColorHelper;
import com.crschnick.pdx_unlimiter.core.node.ArrayNode;
import com.crschnick.pdx_unlimiter.core.node.LinkedNode;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;
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
        update(ArrayNode.array(List.of(new ValueNode(val))));
    }

    public void updateColor(Color c) {
        update(ArrayNode.array(List.of(ColorHelper.toColorNode(c))));
    }

    public void insertArray(ArrayNode toInsert, int beginIndex, int endIndex) {
        ArrayNode ar = (ArrayNode) getRealParent().getBackingNode();

        var begin = ar.splice(0, beginIndex);
        var end = ar.splice(endIndex, ar.getNodeArray().size() - endIndex);
        var linked = new LinkedNode(List.of(begin, toInsert, end));

        // Update parent node to reflect change
        if (getDirectParent() != null) {
            getRealParent().getBackingNode().getNodeArray().set(getKeyIndex(), linked);
        }
        this.backingNode = linked;
    }

    @Override
    public void delete() {
        if (getDirectParent() != null) {
            getRealParent().getBackingNode().getNodeArray().remove(getKeyIndex());
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
    public boolean filterValue(Predicate<String> filter) {
        return false;
    }

    @Override
    public String displayKeyName() {
        return getKeyName().orElse("[" + keyIndex + "]");
    }

    @Override
    public String navigationName() {
        return getKeyName().orElseGet(() -> getDirectParent().navigationName() + "[" + keyIndex + "]");
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
    public List<EditorNode> open() {
        return EditorNode.create(this, (ArrayNode) backingNode);
    }

    public Node toWritableNode() {
        return backingNode;
    }

    public void update(ArrayNode newNode) {
        Node nodeToUse = backingNode instanceof ArrayNode ? newNode : newNode.getNodeArray().get(0);

        // Update parent node to reflect change
        if (getDirectParent() != null) {
            getRealParent().getBackingNode().getNodeArray().set(getKeyIndex(), nodeToUse);
        }
        this.backingNode = nodeToUse;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public Node getBackingNode() {
        return backingNode;
    }
}
