package com.crschnick.pdxu.app.editor;


import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.node.ValueNode;

import java.util.Optional;

public class EditorNodePointers {

    public static Optional<NodePointer> createEu4(EditorState state, EditorSimpleNode node) {
        var n = node.getBackingNode();
        if (n.isValue()) {
            var s = n.getString();
            if (s.length() == 3 && Character.isLetter(s.charAt(0)) && s.toUpperCase().equals(s)) {
                return Optional.of(NodePointer.builder().name("countries").name(s).build());
            }

            if (!((ValueNode) n).isQuoted()) {
                var religion = NodePointer.builder().name("religions").name(s).build();
                return Optional.of(religion);
            }
        }

        return Optional.empty();
    }

    public static NodePointer createCk3(EditorState state, EditorSimpleNode node) {
        var keyOpt = node.getKeyName();
        if (keyOpt.isPresent()) {
            var key = keyOpt.get();
            if ((key.equals("dynasty_house") || key.equals("dynasty")) && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("dynasties").name("dynasty_house")
                        .name(node.getBackingNode().getString()).build();
            }
            if (key.equals("culture") && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("culture_manager").name("cultures")
                        .name(node.getBackingNode().getString()).build();
            }
            if (key.equals("coat_of_arms_id") && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("coat_of_arms").name("coat_of_arms_manager_database")
                        .name(node.getBackingNode().getString()).build();
            }
            if ((key.equals("dynasty_head") || key.equals("head_of_house")) && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("living").name(node.getBackingNode().getString()).build();
            }
        }
        return null;
    }

    public static Optional<NodePointer> create(EditorState state, EditorSimpleNode node) {
        if (state.getFileContext().getGame() == Game.EU4) {
            return createEu4(state, node);
        }

        if (state.getFileContext().getGame() == Game.CK3) {
            return Optional.ofNullable(createCk3(state, node));
        }

        return Optional.empty();
    }
}