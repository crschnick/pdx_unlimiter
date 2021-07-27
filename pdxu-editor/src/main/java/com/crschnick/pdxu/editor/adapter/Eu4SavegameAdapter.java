package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.io.node.NodePointer;

import java.util.List;
import java.util.Map;

public class Eu4SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.EU4;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        return Map.of("Player country", NodePointer.builder().name("countries")
                .pointerEvaluation(NodePointer.builder().name("player").build()).build());
    }

    private static final List<String> EU4_PROVINCE_KEYS = List.of("capital", "original_capital", "trade_port");
    private static final List<String> EU4_RELIGION_KEYS = List.of("religion", "dominant_religion");
    private static final List<String> EU4_TRADE_KEYS = List.of("trade");
    private static final List<String> EU4_POWER_NAMES = List.of("ADM", "DIP", "MIL");

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        var keyOpt = node.getKeyName();
        var n = node.getBackingNode();
        if (n.isValue()) {
            var s = n.getString();
            if (s.length() == 3 && Character.isLetter(s.charAt(0)) && s.toUpperCase().equals(s) && !EU4_POWER_NAMES.contains(s)) {
                return NodePointer.builder().name("countries").name(s).build();
            }
        }

        if (keyOpt.isPresent() && n.isValue()) {
            var key = keyOpt.get();
            var s = n.getString();
            if (EU4_PROVINCE_KEYS.contains(key)) {
                return NodePointer.builder().name("provinces").name("-" + s).build();
            }

            if (EU4_RELIGION_KEYS.contains(key)) {
                return NodePointer.builder().name("religions").name(s).build();
            }

            if (EU4_TRADE_KEYS.contains(key)) {
                return NodePointer.builder().name("trade").name("node").selector(tn -> tn.hasKey("definitions") &&
                        tn.getNodeForKey("definitions").getString().equals(s)).build();
            }
        }
        return null;
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node) {
        return null;
    }
}
