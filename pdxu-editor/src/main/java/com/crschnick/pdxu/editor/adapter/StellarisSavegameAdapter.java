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
        map.put("Galactic community", NodePointer.builder().name("galactic_community").build());
        map.put("Wars", NodePointer.builder().name("war").build());

        var playerId =
                NodePointer.builder().name("player").index(0).name("country").build();
        var country =
                NodePointer.builder().name("country").pointerEvaluation(playerId).build();
        map.put("Player country", country);
        map.put(
                "Player resources",
                NodePointer.fromBase(country)
                        .name("modules")
                        .name("standard_economy_module")
                        .name("resources")
                        .build());
        map.put("Player budget", NodePointer.fromBase(country).name("budget").build());
        map.put("Player government", NodePointer.fromBase(country).name("government").build());
        map.put("Player technology", NodePointer.fromBase(country).name("tech_status").build());
        map.put(
                "Player ruler",
                NodePointer.builder()
                        .name("leaders")
                        .pointerEvaluation(
                                NodePointer.fromBase(country).name("ruler").build())
                        .build());
        map.put(
                "Player species",
                NodePointer.builder()
                        .name("species_db")
                        .pointerEvaluation(NodePointer.fromBase(country)
                                .name("founder_species_ref")
                                .build())
                        .build());
        map.put(
                "Player federation",
                NodePointer.builder()
                        .name("federation")
                        .pointerEvaluation(
                                NodePointer.fromBase(country).name("federation").build())
                        .build());
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
