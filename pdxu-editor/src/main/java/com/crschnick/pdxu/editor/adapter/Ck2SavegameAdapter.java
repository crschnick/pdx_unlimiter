package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.NodePointer;

import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Ck2SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.CK2;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var playerId = NodePointer.builder().name("player").name("id").build();
        var player = NodePointer.builder()
                .name("character")
                .pointerEvaluation(playerId)
                .build();
        var realm = NodePointer.builder().name("player_realm").build();

        var map = new LinkedHashMap<String, NodePointer>();
        map.put("Game rules", NodePointer.builder().name("game_rules").build());
        map.put("Player character", player);
        map.put("Player demesne", NodePointer.fromBase(player).name("dmn").build());
        map.put("Player claims", NodePointer.fromBase(player).name("claim").build());
        map.put(
                "Player dynasty",
                NodePointer.builder()
                        .name("dynasties")
                        .pointerEvaluation(NodePointer.fromBase(player).name("dnt").build())
                        .build());
        map.put(
                "Player primary title",
                NodePointer.builder().name("title").pointerEvaluation(realm).build());
        map.put("First active war", NodePointer.builder().name("war").build());
        return map;
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        if (!state.isSavegame()) {
            return null;
        }

        return EditorSavegameAdapter.keyValueJump(node, (key, val) -> {
            if (CHARACTER_KEYS.contains(key) && val.matches("[1-9][0-9]*")) {
                return NodePointer.builder().name("character").name(val).build();
            }
            if (key.equals("dnt") && val.matches("[1-9][0-9]*")) {
                return NodePointer.builder().name("dynasties").name(val).build();
            }
            if (TITLE_KEYS.contains(key) && val.matches("[ekdcb]_.+")) {
                return NodePointer.builder().name("title").name(val).build();
            }
            if (key.equals("rel")) {
                return NodePointer.builder().name("religion").name(val).build();
            }
            return null;
        });
    }

    private static final Set<String> CHARACTER_KEYS =
            Set.of("fat", "mot", "spouse", "emp", "host", "holder", "killer", "lover", "guardian");
    private static final Set<String> TITLE_KEYS = Set.of("liege", "de_jure_liege", "capital", "title");

    @Override
    public Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
