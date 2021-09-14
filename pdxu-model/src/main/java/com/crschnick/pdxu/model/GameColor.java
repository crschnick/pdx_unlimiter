package com.crschnick.pdxu.model;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.TaggedNode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class GameColor {

    public static final GameColor BLACK = new GameColor(TaggedNode.TagType.RGB, List.of("0", "0", "0"));

    private TaggedNode.TagType type;
    private List<String> values;

    public GameColor() {
    }
    public GameColor(TaggedNode.TagType type, List<String> values) {
        this.type = type;
        this.values = values;
    }

    public static GameColor fromRgbArray(Node node) {
        return new GameColor(TaggedNode.TagType.RGB, node.getNodeArray().stream()
                .map(Node::getString)
                .collect(Collectors.toList()));
    }

    public static GameColor fromColorNode(Node n) {
        if (!n.isTagged() || Arrays.binarySearch(TaggedNode.COLORS, n.getTaggedNode().getType(), null) == -1) {
            throw new IllegalArgumentException("Invalid color node " + n.toString());
        }

        return new GameColor(n.getTaggedNode().getType(), n.getTaggedNode().getValues().stream()
                .map(Node::getString)
                .collect(Collectors.toList()));
    }

    public TaggedNode.TagType getType() {
        return type;
    }

    public List<String> getValues() {
        return values;
    }
}
