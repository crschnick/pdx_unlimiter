package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Eu5SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.EU5;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var map = new LinkedHashMap<String, NodePointer>();
        return map;
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        return null;
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
