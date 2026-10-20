package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.NodePointer;

import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.List;
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
        map.put("Player policies", NodePointer.fromBase(country).name("active_policies").build());
        map.put("Player traditions", NodePointer.fromBase(country).name("traditions").build());
        map.put("Player ascension perks", NodePointer.fromBase(country).name("ascension_perks").build());
        map.put("Player edicts", NodePointer.fromBase(country).name("edicts").build());
        map.put("Player sectors", NodePointer.fromBase(country).name("sectors").build());
        map.put("Player fleets", NodePointer.fromBase(country).name("fleets_manager").build());
        map.put("Player relations", NodePointer.fromBase(country).name("relations_manager").build());
        map.put("Player planets", NodePointer.fromBase(country).name("owned_planets").build());
        map.put(
                "Player capital",
                NodePointer.builder()
                        .name("planets")
                        .name("planet")
                        .pointerEvaluation(NodePointer.fromBase(country).name("capital").build())
                        .build());
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
        if (!state.isSavegame()) {
            return null;
        }

        var pointer = EditorSavegameAdapter.keyValueJump(node, (key, val) -> {
            var path = DATABASES.get(key);
            if (path == null || !val.matches("[0-9]+")) {
                return null;
            }

            var builder = NodePointer.builder();
            path.forEach(builder::name);
            return builder.name(val).build();
        });
        return pointer != null && pointer.isValid(state.getBackingNode()) ? pointer : null;
    }

    private static final Map<String, List<String>> DATABASES = Map.ofEntries(
            Map.entry("owner", List.of("country")),
            Map.entry("controller", List.of("country")),
            Map.entry("country", List.of("country")),
            Map.entry("original_owner", List.of("country")),
            Map.entry("planet", List.of("planets", "planet")),
            Map.entry("capital", List.of("planets", "planet")),
            Map.entry("system", List.of("galactic_object")),
            Map.entry("galactic_object", List.of("galactic_object")),
            Map.entry("origin", List.of("galactic_object")),
            Map.entry("fleet", List.of("fleet")),
            Map.entry("ship", List.of("ships")),
            Map.entry("leader", List.of("leaders")),
            Map.entry("ruler", List.of("leaders")),
            Map.entry("species", List.of("species_db")),
            Map.entry("species_index", List.of("species_db")),
            Map.entry("founder_species_ref", List.of("species_db")),
            Map.entry("army", List.of("army")),
            Map.entry("federation", List.of("federation")),
            Map.entry("starbase", List.of("starbase_mgr", "starbases")),
            Map.entry("sector", List.of("sectors")),
            Map.entry("war", List.of("war")),
            Map.entry("megastructure", List.of("megastructures")));

    @Override
    public Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return null;
    }
}
