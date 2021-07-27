package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.Map;

public class Hoi4SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.HOI4;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        return Map.of();
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        return null;
    }

    @Override
    public Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
