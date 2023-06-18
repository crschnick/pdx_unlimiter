package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.Map;

public class StellarisSavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.STELLARIS;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var map = new LinkedHashMap<String, NodePointer>();

        map.put("DLCs", NodePointer.builder().name("required_dlcs").build());
        map.put("Galaxy", NodePointer.builder().name("galaxy").build());

        var country = NodePointer.builder().name("country").name("0").build();
        map.put("Player country", country);

        return map;
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
