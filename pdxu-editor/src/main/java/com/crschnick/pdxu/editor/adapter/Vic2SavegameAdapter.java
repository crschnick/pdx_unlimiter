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

public class Vic2SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.VIC2;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var playerTag = NodePointer.builder().name("player").build();
        var player = NodePointer.builder().pointerEvaluation(playerTag).build();

        var map = new LinkedHashMap<String, NodePointer>();
        map.put("Game settings", NodePointer.builder().name("gameplaysettings").build());
        map.put("Player country", player);
        map.put(
                "Player capital",
                NodePointer.builder()
                        .pointerEvaluation(NodePointer.fromBase(player).name("capital").build())
                        .build());
        map.put("Player technology", NodePointer.fromBase(player).name("technology").build());
        map.put("Player bank", NodePointer.fromBase(player).name("bank").build());
        map.put("Diplomacy", NodePointer.builder().name("diplomacy").build());
        map.put("First active war", NodePointer.builder().name("active_war").build());
        map.put("World market", NodePointer.builder().name("worldmarket").build());
        return map;
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        if (!state.isSavegame()) {
            return null;
        }

        return EditorSavegameAdapter.keyValueJump(node, (key, val) -> {
            if (TAG_KEYS.contains(key) && val.matches("[A-Z][A-Z0-9]{2}")) {
                return NodePointer.builder().name(val).build();
            }
            if (PROVINCE_KEYS.contains(key) && val.matches("[1-9][0-9]*")) {
                return NodePointer.builder().name(val).build();
            }
            return null;
        });
    }

    private static final Set<String> TAG_KEYS = Set.of(
            "owner",
            "controller",
            "core",
            "country",
            "tag",
            "first",
            "second",
            "attacker",
            "defender",
            "original_attacker",
            "original_defender");
    private static final Set<String> PROVINCE_KEYS = Set.of("capital", "location", "province");

    @Override
    public Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
