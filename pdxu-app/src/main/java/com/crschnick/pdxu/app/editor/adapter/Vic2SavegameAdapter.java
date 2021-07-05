package com.crschnick.pdxu.app.editor.adapter;

import com.crschnick.pdxu.app.editor.node.EditorSimpleNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.scene.Node;

import java.util.Map;

public class Vic2SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.VIC2;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        return Map.of();
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorSimpleNode node) {
        return null;
    }

    @Override
    public Node createNodeTag(EditorState state, EditorSimpleNode node) {
        return null;
    }
}
