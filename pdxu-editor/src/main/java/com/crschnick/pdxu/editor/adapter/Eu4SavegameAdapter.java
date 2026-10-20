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

public class Eu4SavegameAdapter implements EditorSavegameAdapter {

    @Override
    public Game getGame() {
        return Game.EU4;
    }

    private NodePointer personPointer(EditorState state, NodePointer country, NodePointer personId) {
        Predicate<Node> personPred = dateEntry -> {
            if (!dateEntry.isArray()) {
                return false;
            }

            var idn = dateEntry.getNodeForKeyIfExistent("id");
            var id = personId.get(state.getBackingNode());
            if (id != null && id.isValue() && idn.isPresent()) {
                var nodeId = idn.get()
                        .getNodeForKeyIfExistent("id")
                        .filter(Node::isValue)
                        .map(Node::getString)
                        .orElse("");
                if (nodeId.equals(id.getString())) {
                    return true;
                }
            }
            return false;
        };

        return NodePointer.fromBase(country)
                .name("history")
                .selector(node -> {
                    if (node.isArray()) {
                        for (Node dateEntry : node.getNodeArray()) {
                            if (personPred.test(dateEntry)) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .selector(personPred)
                .build();
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var map = new LinkedHashMap<String, NodePointer>();

        map.put("Mods", NodePointer.builder().name("mods_enabled_names").build());
        map.put("DLCs", NodePointer.builder().name("dlc_enabled").build());
        map.put(
                "Settings",
                NodePointer.builder()
                        .name("gameplaysettings")
                        .name("setgameplayoptions")
                        .build());

        var country = NodePointer.builder()
                .name("countries")
                .pointerEvaluation(NodePointer.builder().name("player").build())
                .build();
        map.put("Player country", country);

        var rulerId = NodePointer.fromBase(country).name("monarch").name("id").build();
        var heirId = NodePointer.fromBase(country).name("heir").name("id").build();
        map.put("Player country ruler", personPointer(state, country, rulerId));
        map.put("Player country heir", personPointer(state, country, heirId));

        map.put("Player government", NodePointer.fromBase(country).name("government").build());
        map.put("Player technology", NodePointer.fromBase(country).name("technology").build());
        map.put("Player idea groups", NodePointer.fromBase(country).name("active_idea_groups").build());
        map.put("Player ledger", NodePointer.fromBase(country).name("ledger").build());
        map.put(
                "Player capital",
                NodePointer.builder()
                        .name("provinces")
                        .function((root, n) -> {
                            var capital = NodePointer.fromBase(country).name("capital").build().get(root);
                            return capital != null ? "-" + capital.getString() : null;
                        })
                        .build());
        map.put("Diplomacy", NodePointer.builder().name("diplomacy").build());
        map.put("First active war", NodePointer.builder().name("active_war").build());
        map.put("Great powers", NodePointer.builder().name("great_powers").build());
        map.put("Holy Roman Empire", NodePointer.builder().name("empire").build());
        map.put("Celestial Empire", NodePointer.builder().name("celestial_empire").build());
        map.put("Religions", NodePointer.builder().name("religions").build());
        map.put("Trade", NodePointer.builder().name("trade").build());

        return map;
    }

    private static final List<String> EU4_PROVINCE_KEYS =
            List.of("capital", "original_capital", "trade_port", "location", "province", "home", "previous");
    private static final List<String> EU4_RELIGION_KEYS = List.of("religion", "dominant_religion");
    private static final List<String> EU4_TRADE_KEYS = List.of("trade");
    private static final List<String> EU4_POWER_NAMES = List.of("ADM", "DIP", "MIL");

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        var keyOpt = node.getKeyName();
        var n = node.getBackingNode();
        if (n.isValue()) {
            var s = n.getString();
            if (s.length() == 3
                    && Character.isLetter(s.charAt(0))
                    && s.toUpperCase().equals(s)
                    && !EU4_POWER_NAMES.contains(s)) {
                return NodePointer.builder().name("countries").name(s).build();
            }
        }

        if (keyOpt.isPresent() && n.isValue()) {
            var key = keyOpt.get();
            var s = n.getString();
            if (EU4_PROVINCE_KEYS.contains(key) && s.matches("[1-9][0-9]*")) {
                return NodePointer.builder().name("provinces").name("-" + s).build();
            }

            if (EU4_RELIGION_KEYS.contains(key)) {
                return NodePointer.builder().name("religions").name(s).build();
            }

            if (EU4_TRADE_KEYS.contains(key)) {
                return NodePointer.builder()
                        .name("trade")
                        .name("node")
                        .selector(tn -> tn.isArray()
                                && tn.hasKey("definitions")
                                && tn.getNodeForKey("definitions").getString().equals(s))
                        .build();
            }
        }
        return null;
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
