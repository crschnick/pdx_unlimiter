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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class Hoi4SavegameAdapter implements EditorSavegameAdapter {

    private static final List<String> CHARACTER_GROUPS = List.of("historical", "dynamic");

    @Override
    public Game getGame() {
        return Game.HOI4;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var playerTag = NodePointer.builder().name("player").build();
        var country =
                NodePointer.builder().name("countries").pointerEvaluation(playerTag).build();

        var map = new LinkedHashMap<String, NodePointer>();
        map.put("Settings", NodePointer.builder().name("game_rules").build());
        map.put("Difficulty settings", NodePointer.builder().name("difficulty_settings").build());
        map.put("Mods", NodePointer.builder().name("mods").build());
        map.put("Player country", country);
        map.put("Player politics", NodePointer.fromBase(country).name("politics").build());
        map.put("Player diplomacy", NodePointer.fromBase(country).name("diplomacy").build());
        map.put("Player units", NodePointer.fromBase(country).name("units").build());
        map.put("Player technology", NodePointer.fromBase(country).name("technology").build());
        map.put("Player production", NodePointer.fromBase(country).name("production").build());
        map.put("Player focus", NodePointer.fromBase(country).name("focus").build());
        map.put("Player resources", NodePointer.fromBase(country).name("resources").build());
        map.put("Player buildings", NodePointer.fromBase(country).name("buildings").build());
        map.put("Player characters", NodePointer.fromBase(country).name("characters").build());
        map.put(
                "Player intelligence agency",
                NodePointer.fromBase(country).name("intelligence_agency").build());
        map.put(
                "Player capital",
                NodePointer.builder()
                        .name("states")
                        .pointerEvaluation(NodePointer.fromBase(country).name("capital").build())
                        .build());

        var root = state.getBackingNode();
        map.put("Player country leader", createLeaderPointer(root, country.get(root)));

        var tag = Optional.ofNullable(playerTag.get(root))
                .filter(Node::isValue)
                .map(Node::getString)
                .orElse(null);
        map.put("Player faction", tag != null ? createFactionPointer(tag) : null);
        return map;
    }

    private static NodePointer createLeaderPointer(Node root, Node country) {
        if (country == null) {
            return null;
        }

        var leaderId = Optional.of(country)
                .flatMap(c -> c.getNodeForKeyIfExistent("politics"))
                .flatMap(politics -> politics.getNodeForKeyIfExistent("ruling_party")
                        .filter(Node::isValue)
                        .map(Node::getString)
                        .flatMap(party -> politics.getNodeForKeyIfExistent("parties")
                                .flatMap(parties -> parties.getNodeForKeyIfExistent(party))))
                .flatMap(party -> party.getNodeForKeyIfExistent("country_leader"))
                .filter(Node::isArray)
                .flatMap(leaders -> leaders.getNodeArray().stream().findFirst())
                .flatMap(leader -> leader.getNodeForKeyIfExistent("character"))
                .flatMap(character -> character.getNodeForKeyIfExistent("id"))
                .filter(Node::isValue)
                .map(Node::getString);
        if (leaderId.isEmpty()) {
            return null;
        }

        Predicate<Node> isLeader = c -> c.isArray()
                && c.getNodeForKeyIfExistent("id")
                        .flatMap(id -> id.getNodeForKeyIfExistent("id"))
                        .filter(Node::isValue)
                        .map(Node::getString)
                        .map(leaderId.get()::equals)
                        .orElse(false);

        var characters = root.getNodeForKeyIfExistent("character_manager");
        for (var group : CHARACTER_GROUPS) {
            var groupNode = characters.flatMap(c -> c.getNodeForKeyIfExistent(group));
            if (groupNode.isPresent() && groupNode.get().getNodeArray().stream().anyMatch(isLeader)) {
                return NodePointer.builder()
                        .name("character_manager")
                        .name(group)
                        .selector(isLeader)
                        .build();
            }
        }
        return null;
    }

    private static NodePointer createFactionPointer(String tag) {
        return NodePointer.builder()
                .selector(n -> n.isArray()
                        && n.hasKey("faction_leader_change_date")
                        && n.getNodeForKeyIfExistent("members")
                                .filter(Node::isArray)
                                .map(members -> members.getNodeArray().stream()
                                        .anyMatch(m -> m.isValue() && m.getString().equals(tag)))
                                .orElse(false))
                .build();
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        if (!state.isSavegame()) {
            return null;
        }

        return EditorSavegameAdapter.keyValueJump(node, (key, val) -> {
            if (TAG_KEYS.contains(key) && val.matches("[A-Z][A-Z0-9]{2}")) {
                return NodePointer.builder().name("countries").name(val).build();
            }
            if (!val.matches("[1-9][0-9]*")) {
                return null;
            }
            if (STATE_KEYS.contains(key)) {
                return NodePointer.builder().name("states").name(val).build();
            }
            if (PROVINCE_KEYS.contains(key)) {
                return NodePointer.builder().name("provinces").name(val).build();
            }
            return null;
        });
    }

    private static final Set<String> TAG_KEYS =
            Set.of("owner", "controller", "tag", "original_tag", "country", "player");
    private static final Set<String> STATE_KEYS = Set.of("state", "capital", "original_capital");
    private static final Set<String> PROVINCE_KEYS = Set.of("province", "location");

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
