package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.editor.gui.GuiCk3CoaViewer;
import com.crschnick.pdxu.editor.gui.GuiEditorNodeTagFactory;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodePointer;
import com.jfoenix.controls.JFXButton;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.*;

public class Ck3SavegameAdapter implements EditorSavegameAdapter {

    static class CoaPreview extends GuiEditorNodeTagFactory {
        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            if (!state.isContextGameEnabled()) {
                return false;
            }

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

        public ImagePreview(Path base, String nodeName, String icon) {
            super(icon, node -> GameInstallation.ALL.get(Game.CK3).getInstallDir().resolve("game")
                    .resolve(base).resolve(node.getBackingNode().getString()));
            this.nodeName = nodeName;
        }

        @Override
        public boolean checkIfApplicable(EditorState state, EditorRealNode node) {
            return state.isContextGameEnabled() && node.getKeyName().map(k -> k.equals(nodeName)).orElse(false);
        }
    }

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(
            new CoaPreview(),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("patterns"), "pattern", "mdi-file"),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("colored_emblems"), "texture", "mdi-file"),
            new GuiEditorNodeTagFactory.InfoNodeTagFactory(Set.of("meta_data"),
                    "Contains the meta data of this savegame that is shown in the main menu. " +
                            "Editing anything inside of it only changes the main menu display, not the actual data in-game."));

    @Override
    public Game getGame() {
        return Game.CK3;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var player = NodePointer.builder().name("currently_played_characters").index(0).build();
        var playerDyn = NodePointer.builder()
                .name("living").pointerEvaluation(state.getBackingNode(), player).name("dynasty_house").build();

        var map = new LinkedHashMap<String, NodePointer>();
        map.put("All player characters", NodePointer.builder().name("currently_played_characters").build());
        map.put("Player character", NodePointer.builder().name("living").pointerEvaluation(state.getBackingNode(), player).build());
        map.put("Player realm", NodePointer.builder().name("living").pointerEvaluation(state.getBackingNode(), player)
                .name("landed_data").name("domain").build());
        map.put("Player house/dynasty", NodePointer.builder().name("dynasties").name("dynasty_house")
                .pointerEvaluation(state.getBackingNode(), playerDyn).build());
        var primaryTitle = NodePointer.builder().name("landed_titles").name("landed_titles").pointerEvaluation(
                state.getBackingNode(),
                NodePointer.builder().name("living").pointerEvaluation(state.getBackingNode(), player)
                        .name("landed_data").name("domain").index(0).build()
        ).build();
        map.put("Player primary title", primaryTitle);
        var primaryTitleCoaId = NodePointer.fromBase(primaryTitle).name("coat_of_arms_id").build();
        var primaryTitleCoa = NodePointer.builder().name("coat_of_arms")
                .name("coat_of_arms_manager_database").pointerEvaluation(state.getBackingNode(), primaryTitleCoaId).build();
        map.put("Player primary title coat of arms", primaryTitleCoa);
        return map;
    }

    private static final List<String> DYNASTY_KEYS = List.of("dynasty_house", "dynasty", "historical");
    private static final List<String> CULTURE_KEYS = List.of("culture");
    private static final List<String> FAITH_KEYS = List.of("faith");
    private static final List<String> RELIGION_KEYS = List.of("religion");
    private static final List<String> COA_KEYS = List.of("coat_of_arms_id");
    private static final List<String> HOLY_SITE_KEYS = List.of("holy_sites");
    private static final List<String> LIVING_KEYS = List.of(
            "dynasty_head", "head_of_house", "religious_head", "holder", "owner", "character",
            "target", "attacker", "defender", "claimant", "first", "second", "head",
            "council", "child", "heir", "succession", "vassal_contracts", "claim", "de_jure_vassals",
            "currently_played_characters", "knights", "spouse", "primary_spouse", "kills",
            "ruler_designer_characters", "former_spouses", "participants");
    private static final List<String> PROVINCE_KEYS = List.of("capital", "origin", "province", "location", "realm_capital", "diplo_centers");
    private static final List<String> COUNTY_KEYS = List.of("county");
    private static final List<String> ARMY_KEYS = List.of("army");
    private static final List<String> TITLE_KEYS = List.of("targeted_titles", "title", "domain");
    private static final List<String> STORIES_KEYS = List.of("stories");
    private static final List<String> SCHEMES_KEYS = List.of("schemes");
    private static final List<String> REGIMENTS_KEYS = List.of("regiments");
    private static final List<String> SECRETS_KEYS = List.of("secret", "secrets", "targeting_secrets");


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
            return NodePointer.builder().name("religion").name("holy_sites").name(val).build();
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
        if (STORIES_KEYS.contains(key)) {
            return NodePointer.builder().name("stories").name("active").name(val).build();
        }
        if (SCHEMES_KEYS.contains(key)) {
            return NodePointer.builder().name("schemes").name("active").name(val).build();
        }
        if (REGIMENTS_KEYS.contains(key)) {
            return NodePointer.builder().name("armies").name("regiments").name(val).build();
        }
        if (SECRETS_KEYS.contains(key)) {
            return NodePointer.builder().name("secrets").name("secrets").name(val).build();
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
