package com.crschnick.pdxu.app.editor;


import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.io.node.NodePointer;

import java.util.List;
import java.util.Optional;

public class EditorNodePointers {

    private static final List<String> EU4_PROVINCE_KEYS = List.of("capital", "original_capital", "trade_port");
    private static final List<String> EU4_RELIGION_KEYS = List.of("religion", "dominant_religion");
    private static final List<String> EU4_TRADE_KEYS = List.of("trade");
    private static final List<String> EU4_POWER_NAMES = List.of("ADM", "DIP", "MIL");

    public static NodePointer createEu4(EditorState state, EditorSimpleNode node) {
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

    public static NodePointer createCk3(EditorState state, EditorSimpleNode node) {
        var keyOpt = node.getKeyName();
        if (keyOpt.isPresent() && node.getBackingNode().isValue()) {
            var key = keyOpt.get();
            if ((key.equals("dynasty_house") || key.equals("dynasty")) && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("dynasties").name("dynasty_house")
                        .name(node.getBackingNode().getString()).build();
            }
            if (key.equals("culture") && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("culture_manager").name("cultures")
                        .name(node.getBackingNode().getString()).build();
            }
            if (key.equals("faith") && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("religion").name("faiths")
                        .name(node.getBackingNode().getString()).build();
            }
            if (key.equals("religion") && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("religion").name("religions")
                        .name(node.getBackingNode().getString()).build();
            }
            if (key.equals("coat_of_arms_id") && node.getBackingNode().isValue()) {
                return NodePointer.builder().name("coat_of_arms").name("coat_of_arms_manager_database")
                        .name(node.getBackingNode().getString()).build();
            }
            if (List.of("dynasty_head", "head_of_house", "religious_head", "holder", "owner", "character",
                    "target", "attacker", "defender", "claimant", "first", "second").contains(key)) {
                return NodePointer.builder().name("living").name(node.getBackingNode().getString()).build();
            }
            if (List.of("capital", "origin", "province", "location", "realm_capital").contains(key)) {
                return NodePointer.builder().name("provinces").name(node.getBackingNode().getString()).build();
            }
            if (List.of("county").contains(key)) {
                return NodePointer.builder().name("county_manager").name(node.getBackingNode().getString()).build();
            }
            if (List.of("army").contains(key)) {
                return NodePointer.builder().name("units").name(node.getBackingNode().getString()).build();
            }
            if (List.of("targeted_titles", "title").contains(key)) {
                return NodePointer.builder().name("landed_titles").name("landed_titles").name(node.getBackingNode().getString()).build();
            }
        }

        var parentKey = Optional.ofNullable(node.getDirectParent())
                .flatMap(p -> p.getKeyName());
        if (parentKey.isPresent() && node.getBackingNode().isValue()) {
            var pk = parentKey.get();
            if (pk.equals("holy_sites")) {
                return NodePointer.builder().name("religions").name("holy_sites").name(node.getBackingNode().getString()).build();
            }
            if (List.of("council", "child", "heir", "succession", "vassal_contracts", "claim", "de_jure_vassals", "currently_played_characters", "knights").contains(pk)) {
                return NodePointer.builder().name("living").name(node.getBackingNode().getString()).build();
            }
            if (pk.equals("domain")) {
                return NodePointer.builder().name("landed_titles").name("landed_titles").name(node.getBackingNode().getString()).build();
            }
            if (List.of("diplo_centers").contains(pk)) {
                return NodePointer.builder().name("provinces").name(node.getBackingNode().getString()).build();
            }
        }

        return null;
    }

    public static Optional<NodePointer> create(EditorState state, EditorSimpleNode node) {
        if (state.getFileContext().getGame() == Game.EU4) {
            return Optional.ofNullable(createEu4(state, node));
        }

        if (state.getFileContext().getGame() == Game.CK3) {
            return Optional.ofNullable(createCk3(state, node));
        }

        return Optional.empty();
    }
}