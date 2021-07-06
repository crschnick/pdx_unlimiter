package com.crschnick.pdxu.app.editor.adapter;

import com.crschnick.pdxu.app.editor.node.EditorRealNode;
import com.crschnick.pdxu.app.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.editor.GuiCk3CoaViewer;
import com.crschnick.pdxu.app.gui.editor.GuiEditorNodeTagFactory;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodePointer;
import com.jfoenix.controls.JFXButton;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ck3SavegameAdapter implements EditorSavegameAdapter {

    static class CoaPreview extends GuiEditorNodeTagFactory {
        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            if (node.getBackingNode().isArray()) {
                ArrayNode ar = (ArrayNode) node.getBackingNode();
                return ar.hasKey("pattern");
            }
            return false;
        }

        @Override
        public javafx.scene.Node create(EditorState state, EditorRealNode node) {
            var b = new JFXButton();
            b.setGraphic(new FontIcon());
            b.getStyleClass().add("coa-button");
            GuiTooltips.install(b, "Open in coat of arms preview window");
            b.setOnAction(e -> {
                var viewer = new GuiCk3CoaViewer(state, node);
                viewer.createStage();
            });
            return b;
        }
    }

    static class ImagePreview extends GuiEditorNodeTagFactory.ImagePreviewNodeTagFactory {

        private final String nodeName;

        public ImagePreview(Path base, String nodeName) {
            super(node -> GameInstallation.ALL.get(Game.CK3).getInstallDir().resolve("game")
                    .resolve(base).resolve(node.getBackingNode().getString()));
            this.nodeName = nodeName;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            return node.getKeyName().map(k -> k.equals(nodeName)).orElse(false);
        }
    }

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(
            new CoaPreview(),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("patterns"), "pattern"),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("colored_emblems"), "texture"),
            new GuiEditorNodeTagFactory.InfoNodeTagFactory("meta_data",
                    "The meta data of this savegame that is shown in the main menu. " +
                            "Editing anything inside of it only changes the main menu display, not the actual data in-game."));

    @Override
    public Game getGame() {
        return Game.CK3;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var player = state.getBackingNode().getNodeForKey("currently_played_characters").getNodeArray().get(0);
        var playerDyn = state.getBackingNode().getNodeForKeys("living", player.getString(), "dynasty_house");
        var map = new LinkedHashMap<String, NodePointer>();
        map.put("Player character", NodePointer.builder().name("living").name(player.getString()).build());
        map.put("Player realm", NodePointer.builder().name("living").name(player.getString())
                .name("landed_data").name("domain").build());
        map.put("Player house/dynasty", NodePointer.builder().name("dynasties").name("dynasty_house")
                .name(playerDyn.getString()).build());
        return map;
    }

    private static final List<String> DYNASTY_KEYS = List.of("dynasty_house", "dynasty");
    private static final List<String> CULTURE_KEYS = List.of("culture");
    private static final List<String> FAITH_KEYS = List.of("faith");
    private static final List<String> RELIGION_KEYS = List.of("religion");
    private static final List<String> COA_KEYS = List.of("coat_of_arms_id");
    private static final List<String> HOLY_SITE_KEYS = List.of("holy_sites");
    private static final List<String> LIVING_KEYS = List.of(
            "dynasty_head", "head_of_house", "religious_head", "holder", "owner", "character",
            "target", "attacker", "defender", "claimant", "first", "second", "head",
            "council", "child", "heir", "succession", "vassal_contracts", "claim", "de_jure_vassals", "currently_played_characters", "knights");
    private static final List<String> PROVINCE_KEYS = List.of("capital", "origin", "province", "location", "realm_capital", "diplo_centers");
    private static final List<String> COUNTY_KEYS = List.of("county");
    private static final List<String> ARMY_KEYS = List.of("army");
    private static final List<String> TITLE_KEYS = List.of("targeted_titles", "title", "domain");

    private NodePointer get(String key, String val) {
        if (DYNASTY_KEYS.contains(key)) {
            return NodePointer.builder().name("dynasties").name("dynasty_house")
                    .name(val).build();
        }
        if (CULTURE_KEYS.contains(key)) {
            return NodePointer.builder().name("culture_manager").name("cultures")
                    .name(val).build();
        }
        if (FAITH_KEYS.contains(key)) {
            return NodePointer.builder().name("religion").name("faiths")
                    .name(val).build();
        }
        if (RELIGION_KEYS.contains(key)) {
            return NodePointer.builder().name("religion").name("religions")
                    .name(val).build();
        }
        if (COA_KEYS.contains(key)) {
            return NodePointer.builder().name("coat_of_arms").name("coat_of_arms_manager_database")
                    .name(val).build();
        }
        if (HOLY_SITE_KEYS.contains(key)) {
            return NodePointer.builder().name("religions").name("holy_sites").name(val).build();
        }
        if (LIVING_KEYS.contains(key)) {
            return NodePointer.builder().name("living").name(val).build();
        }
        if (PROVINCE_KEYS.contains(key)) {
            return NodePointer.builder().name("provinces").name(val).build();
        }
        if (COUNTY_KEYS.contains(key)) {
            return NodePointer.builder().name("county_manager").name("counties").name(val).build();
        }
        if (ARMY_KEYS.contains(key)) {
            return NodePointer.builder().name("units").name(val).build();
        }
        if (TITLE_KEYS.contains(key)) {
            return NodePointer.builder().name("landed_titles").name("landed_titles").name(val).build();
        }
        
        return null;
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        if (!state.isSavegame()) {
            return null;
        }

        var keyOpt = node.getKeyName();
        if (keyOpt.isPresent() && node.getBackingNode().isValue()) {
            return get(keyOpt.get(), node.getBackingNode().getString());
        }

        if (keyOpt.isPresent() && node.getBackingNode().isArray() && node.getBackingNode().getArrayNode().size() == 1
                && node.getBackingNode().getNodeArray().get(0).isValue()) {
            return get(keyOpt.get(), node.getBackingNode().getNodeArray().get(0).getString());
        }

        var parentKey = Optional.ofNullable(node.getParent())
                .flatMap(p -> p.getKeyName());
        if (parentKey.isPresent() && node.getBackingNode().isValue()) {
            return get(parentKey.get(), node.getBackingNode().getString());
        }
        
        return null;
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node) {
        return FACTORIES.stream()
                .filter(fac -> fac.checkIfApplicable(state, node))
                .findFirst().map(fac -> fac.create(state, node))
                .orElse(null);
    }
}
