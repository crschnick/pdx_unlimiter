package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.gui.GuiCoaViewer;
import com.crschnick.pdxu.editor.gui.GuiCoaViewerState;
import com.crschnick.pdxu.editor.gui.GuiEditorNodeTagFactory;
import com.crschnick.pdxu.editor.node.EditorNode;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodePointer;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;

import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.*;

public class Vic3SavegameAdapter implements EditorSavegameAdapter {

    static class CoaPreview extends GuiEditorNodeTagFactory {
        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            if (!state.isContextGameEnabled()) {
                return false;
            }

            if (node.getBackingNode().isArray()) {
                ArrayNode ar = (ArrayNode) node.getBackingNode();
                return ar.hasKey("pattern") || ar.hasKey("colored_emblem");
            }
            return false;
        }

        @Override
        public javafx.scene.Node create(EditorState state, EditorRealNode node, Region valueDisplay) {
            var b = new Button(null, new FontIcon());
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            GuiTooltips.install(b, "Open in coat of arms preview window");
            b.setOnAction(e -> {
                var viewer = new GuiCoaViewer<>(new GuiCoaViewerState.Vic3GuiCoaViewerState(state, node));
                viewer.createStage();
            });
            return b;
        }
    }

    static class ImagePreview extends GuiEditorNodeTagFactory.ImagePreviewNodeTagFactory {

        private final String nodeName;

        public ImagePreview(Path base, String nodeName, String icon) {
            super(
                    icon,
                    node -> GameInstallation.ALL
                            .get(Game.VIC3)
                            .getInstallDir()
                            .resolve("game")
                            .resolve(base)
                            .resolve(node.getBackingNode().getString()));
            this.nodeName = nodeName;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            return state.isContextGameEnabled()
                    && node.getKeyName().map(k -> k.equals(nodeName)).orElse(false);
        }
    }

    private static final Set<String> CACHED_KEYS = Set.of("domain_limit");

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(
            new CoaPreview(),
            new GuiEditorNodeTagFactory.CacheTagFactory(CACHED_KEYS),
            new ImagePreview(Path.of("gfx").resolve("coat_of_arms").resolve("patterns"), "pattern", "mdi-file"),
            new ImagePreview(Path.of("gfx").resolve("coat_of_arms").resolve("colored_emblems"), "texture", "mdi-file"),
            new GuiEditorNodeTagFactory.InfoNodeTagFactory(
                    Set.of("meta_data"),
                    "This node contains basic information and the meta data of this savegame that is shown in the main menu. "
                            + "Editing anything inside of it, excluding mods and DLCs, only changes the main menu display, not the actual data in-game. "
                            + "Do not edit this node if you want to change something in-game, except applied mods and DLCs"));

    @Override
    public Game getGame() {
        return Game.VIC3;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var countryId = NodePointer.builder()
                .name("previous_played")
                .index(0)
                .name("idtype")
                .build();
        var country = NodePointer.builder()
                .name("country_manager")
                .name("database")
                .pointerEvaluation(countryId)
                .build();

        var map = new LinkedHashMap<String, NodePointer>();
        map.put("Mods", NodePointer.builder().name("meta_data").name("mods").build());
        map.put("DLCs", NodePointer.builder().name("meta_data").name("dlcs").build());
        map.put(
                "Settings",
                NodePointer.builder().name("game_rules").name("setting").build());
        map.put("Ironman Settings", NodePointer.builder().name("ironman").build());
        map.put("Player country", country);
        map.put("Player budget", NodePointer.fromBase(country).name("budget").build());
        map.put("Player states", NodePointer.fromBase(country).name("states").build());
        map.put(
                "Player capital",
                NodePointer.builder()
                        .name("states")
                        .name("database")
                        .pointerEvaluation(NodePointer.fromBase(country).name("capital").build())
                        .build());
        map.put(
                "Player ruler",
                NodePointer.builder()
                        .name("character_manager")
                        .name("database")
                        .pointerEvaluation(NodePointer.fromBase(country).name("ruler").build())
                        .build());
        map.put(
                "Player heir",
                NodePointer.builder()
                        .name("character_manager")
                        .name("database")
                        .pointerEvaluation(NodePointer.fromBase(country).name("heir").build())
                        .build());
        map.put("Laws", NodePointer.builder().name("laws").name("database").build());
        map.put(
                "Interest groups",
                NodePointer.builder().name("interest_groups").name("database").build());
        map.put("Pacts", NodePointer.builder().name("pacts").name("database").build());
        map.put(
                "Diplomatic plays",
                NodePointer.builder().name("diplomatic_plays").name("database").build());
        map.put("Wars", NodePointer.builder().name("war_manager").name("database").build());
        return map;
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        if (!state.isSavegame()) {
            return null;
        }

        var keyOpt = node.getKeyName();
        var parentKey = Optional.ofNullable(node.getParent()).flatMap(p -> p.getKeyName());

        if (keyOpt.isPresent() && node.getBackingNode().isValue()) {
            return get(node, keyOpt.get(), node.getBackingNode().getString());
        }

        if (keyOpt.isPresent()
                && node.getBackingNode().isArray()
                && node.getBackingNode().getArrayNode().size() == 1
                && node.getBackingNode().getNodeArray().getFirst().isValue()) {
            return get(
                    node,
                    keyOpt.get(),
                    node.getBackingNode().getNodeArray().getFirst().getString());
        }

        if (parentKey.isPresent() && node.getBackingNode().isValue()) {
            return get(node, parentKey.get(), node.getBackingNode().getString());
        }

        return null;
    }

    private static final Map<String, List<String>> DATABASES = Map.ofEntries(
            Map.entry("state", List.of("states", "database")),
            Map.entry("culture", List.of("cultures", "database")),
            Map.entry("country", List.of("country_manager", "database")),
            Map.entry("owner", List.of("country_manager", "database")),
            Map.entry("character", List.of("character_manager", "database")),
            Map.entry("ruler", List.of("character_manager", "database")),
            Map.entry("heir", List.of("character_manager", "database")),
            Map.entry("interest_group", List.of("interest_groups", "database")),
            Map.entry("building", List.of("building_manager", "database")),
            Map.entry("party", List.of("parties", "database")),
            Map.entry("law", List.of("laws", "database")),
            Map.entry("market", List.of("market_manager", "database")),
            Map.entry("war", List.of("war_manager", "database")),
            Map.entry("company", List.of("companies", "database")),
            Map.entry("diplomatic_play", List.of("diplomatic_plays", "database")),
            Map.entry("capital", List.of("provinces")),
            Map.entry("origin", List.of("provinces")),
            Map.entry("province", List.of("provinces")),
            Map.entry("provinces", List.of("provinces")));

    private static boolean isInside(EditorNode node, String key) {
        for (var n = node.getParent(); n != null; n = n.getParent()) {
            if (n.getKeyName().map(key::equals).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    private NodePointer get(EditorRealNode node, String key, String val) {
        if (val.isEmpty() || !val.chars().allMatch(Character::isDigit)) {
            return null;
        }

        var path = key.equals("capital") && isInside(node, "country_manager")
                ? List.of("states", "database")
                : DATABASES.get(key);
        if (path == null) {
            return null;
        }

        var builder = NodePointer.builder();
        path.forEach(builder::name);
        return builder.name(val).build();
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return FACTORIES.stream()
                .filter(fac -> fac.checkIfApplicable(state, node))
                .findFirst()
                .map(fac -> fac.create(state, node, valueDisplay))
                .orElse(null);
    }
}
