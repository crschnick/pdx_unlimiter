package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.core.node.NodePointer;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;

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

    public static Optional<NodePointer> create(EditorState state, EditorSimpleNode node) {
        if (state.getFileContext().getGame() == Game.EU4) {
            return createEu4(state, node).filter(p -> EditorNavPath.createNavPath(state.getRootNodes().values(), p).isPresent());
        }

        return Optional.empty();
    }
}
