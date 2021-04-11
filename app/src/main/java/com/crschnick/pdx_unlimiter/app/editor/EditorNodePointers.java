package com.crschnick.pdx_unlimiter.app.editor;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.core.node.ValueNode;

import java.util.List;
import java.util.Optional;

public class EditorNodePointers {

    public static Optional<EditorNodePointer> createEu4(EditorState state, EditorSimpleNode node) {
        var n = node.getBackingNode();
        if (n.isValue()) {
            var s = n.getString();
            if (s.length() == 3 && Character.isLetter(s.charAt(0)) && s.toUpperCase().equals(s)) {
                return Optional.of(new EditorNodePointer(List.of("countries", s)));
            }

            if (!((ValueNode) n).isQuoted()) {
                var religion = new EditorNodePointer(List.of("religions", s));
                if (religion.isValid(state)) {
                    return Optional.of(religion);
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<EditorNodePointer> create(EditorState state, EditorSimpleNode node) {
        if (state.getFileContext().getGame() == Game.EU4) {
            return createEu4(state, node);
        }

        return Optional.empty();
    }
}
