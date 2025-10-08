package com.crschnick.pdxu.editor.adapter;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.editor.EditorState;
import com.crschnick.pdxu.editor.gui.GuiCoaViewer;
import com.crschnick.pdxu.editor.gui.GuiCoaViewerState;
import com.crschnick.pdxu.editor.gui.GuiEditorNodeTagFactory;
import com.crschnick.pdxu.editor.node.EditorRealNode;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
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
                var viewer = new GuiCoaViewer<>(new GuiCoaViewerState.Ck3GuiCoaViewerState(state, node));
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
    
    private static final Set<String> CACHED_KEYS = Set.of("domain_limit");

    private static final List<GuiEditorNodeTagFactory> FACTORIES = List.of(
            new CoaPreview(),
            new GuiEditorNodeTagFactory.CacheTagFactory(CACHED_KEYS),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("patterns"), "pattern", "mdi-file"),
            new ImagePreview(
                    Path.of("gfx").resolve("coat_of_arms").resolve("colored_emblems"), "texture", "mdi-file"),
            new GuiEditorNodeTagFactory.InfoNodeTagFactory(Set.of("meta_data"),
                    "This node contains basic information and the meta data of this savegame that is shown in the main menu. " +
                            "Editing anything inside of it, excluding mods and DLCs, only changes the main menu display, not the actual data in-game. " +
                            "Do not edit this node if you want to change something in-game, except applied mods and DLCs"));

    @Override
    public Game getGame() {
        return Game.CK3;
    }

    @Override
    public Map<String, NodePointer> createCommonJumps(EditorState state) {
        var player = NodePointer.builder().name("currently_played_characters").index(0).build();
        var playerDyn = NodePointer.builder()
                .name("living").pointerEvaluation(player).name("dynasty_house").build();
        var houseDyn = NodePointer.builder().name("dynasties").name("dynasty_house")
                .pointerEvaluation(playerDyn).build();
        var dynasty = NodePointer.builder().name("dynasties").name("dynasties")
                .pointerEvaluation(NodePointer.fromBase(houseDyn).name("dynasty").build()).build();


        var map = new LinkedHashMap<String, NodePointer>();
        map.put("Mods", NodePointer.builder().name("meta_data").name("mods").build());
        map.put("DLCs", NodePointer.builder().name("meta_data").name("dlcs").build());
        map.put("Settings", NodePointer.builder().name("game_rules").name("setting").build());
        map.put("Ironman Settings", NodePointer.builder().name("ironman_manager").build());
        map.put("All player characters", NodePointer.builder().name("currently_played_characters").build());
        map.put("Player character", NodePointer.builder().name("living").pointerEvaluation(player).build());
        map.put("Player realm", NodePointer.builder().name("living").pointerEvaluation(player)
                .name("landed_data").name("domain").build());
        map.put("Player house", houseDyn);
        map.put("Player dynasty", dynasty);
        var primaryTitle = NodePointer.builder().name("landed_titles").name("landed_titles").pointerEvaluation(
                NodePointer.builder().name("living").pointerEvaluation(player)
                        .name("landed_data").name("domain").index(0).build()
        ).build();
        map.put("Player primary title", primaryTitle);


        var primaryTitleCoaId = NodePointer.fromBase(primaryTitle).name("coat_of_arms_id").build();
        var primaryTitleCoa = NodePointer.builder().name("coat_of_arms")
                .name("coat_of_arms_manager_database").pointerEvaluation(primaryTitleCoaId).build();
        map.put("Player primary title coat of arms", primaryTitleCoa);


        var houseCoaId = NodePointer.builder().name("coat_of_arms")
                .name("coat_of_arms_manager_database").function((root, n) -> {
            var houseCoa = NodePointer.fromBase(houseDyn).name("coat_of_arms_id").build();
            var val = houseCoa.get(root);
            if (val != null) {
                return val.getString();
            }

            var dynCoa = NodePointer.fromBase(dynasty).name("coat_of_arms_id").build();
            val = dynCoa.get(root);
            if (val != null) {
                return val.getString();
            }

            return null;
        }).build();
        map.put("Player house coat of arms", houseCoaId);


        var dynastyCoa = NodePointer.builder().name("coat_of_arms")
                .name("coat_of_arms_manager_database").function((root, n) -> {
                    var dynCoa = NodePointer.fromBase(dynasty).name("coat_of_arms_id").build();
                    var val = dynCoa.get(root);
                    if (val != null) {
                        return val.getString();
                    }

                    return null;
                }).build();
        map.put("Player dynasty coat of arms", dynastyCoa);


        return map;
    }

    private static final List<String> DYNASTY_KEYS = List.of("dynasty_house", "dynasty", "historical");
    private static final List<String> CULTURE_KEYS = List.of("culture");
    private static final List<String> FAITH_KEYS = List.of("faith");
    private static final List<String> RELIGION_KEYS = List.of("religion");
    private static final List<String> COA_KEYS = List.of("coat_of_arms_id");
    private static final List<String> HOLY_SITE_KEYS = List.of("holy_sites");
    private static final List<String> COUNCIL_KEYS = List.of("council");
    private static final List<String> CHARACTER_KEYS = List.of(
            "dynasty_head", "head_of_house", "religious_head", "holder", "owner", "character",
            "target", "attacker", "defender", "claimant", "first", "second", "head",
            "court_owner", "child", "heir", "succession", "claim",
            "currently_played_characters", "knights", "spouse", "primary_spouse", "kills",
            "ruler_designer_characters", "former_spouses", "participants", "last_appointed_councillor", "pretender",
            "vassal", "liege", "employee", "employer", "mother", "father", "assumed_father");
    private static final List<String> PROVINCE_KEYS = List.of("capital", "origin", "province", "location", "realm_capital", "diplo_centers");
    private static final List<String> COUNTY_KEYS = List.of("county");
    private static final List<String> ARMY_KEYS = List.of("army");
    private static final List<String> TITLE_KEYS = List.of("targeted_titles", "title", "domain", "de_jure_liege",
            "de_facto_liege", "de_jure_vassals", "liege_title");
    private static final List<String> STORIES_KEYS = List.of("stories");
    private static final List<String> SCHEMES_KEYS = List.of("schemes");
    private static final List<String> REGIMENTS_KEYS = List.of("regiments");
    private static final List<String> SECRETS_KEYS = List.of("secret", "secrets", "targeting_secrets");
    private static final List<String> VASSAL_CONTRACTS_KEYS = List.of("vassal_contracts");
    private static final List<String> WARS_KEYS = List.of("wars");
    private static final List<String> UNITS_KEYS = List.of("units");
    private static final List<String> INSPIRATIONS_KEYS = List.of("sponsored_inspirations");
    private static final List<String> COURT_POSITIONS_KEYS = List.of("court_positions");




    private NodePointer get(String key, String val) {
        if (DYNASTY_KEYS.contains(key)) {
            return NodePointer.builder().name("dynasties").name("dynasties")
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
        if (COUNCIL_KEYS.contains(key)) {
            return NodePointer.builder().name("council_task_manager").name("active").name(val).build();
        }
        if (CHARACTER_KEYS.contains(key)) {
            return NodePointer.builder().name("living").name(val).build();
        }
        if (PROVINCE_KEYS.contains(key)) {
            return NodePointer.builder().name("provinces").name(val).build();
        }
        if (COUNTY_KEYS.contains(key)) {
            return NodePointer.builder().name("county_manager").name("counties").index(Integer.parseInt(val)).build();
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
        if (VASSAL_CONTRACTS_KEYS.contains(key)) {
            return NodePointer.builder().name("vassal_contracts").name("active").name(val).build();
        }
        if (WARS_KEYS.contains(key)) {
            return NodePointer.builder().name("wars").name("active_wars").name(val).build();
        }
        if (UNITS_KEYS.contains(key)) {
            return NodePointer.builder().name("units").name(val).build();
        }
        if (INSPIRATIONS_KEYS.contains(key)) {
            return NodePointer.builder().name("inspirations_manager").name("inspirations").name(val).build();
        }
        if (COURT_POSITIONS_KEYS.contains(key)) {
            return NodePointer.builder().name("court_positions").name("court_positions").name(val).build();
        }
        
        return null;
    }

    @Override
    public List<NodePointer> createNodeJumps(EditorState state, EditorRealNode node) throws Exception {
        if (!state.isSavegame()) {
            return List.of();
        }

        var keyOpt = node.getKeyName();
        var parentKey = Optional.ofNullable(node.getParent())
                .flatMap(p -> p.getKeyName());

        var isDirectJump = keyOpt.isPresent() && CHARACTER_KEYS.contains(keyOpt.get()) && node.getBackingNode().isValue();
        var isArrayJump = keyOpt.isEmpty() && parentKey.isPresent() && CHARACTER_KEYS.contains(parentKey.get()) && node.getBackingNode().isValue();
        if (isDirectJump || isArrayJump) {
            var livingPointer = NodePointer.builder()
                    .name("living").name(node.getBackingNode().getString()).build();
            var deadPointer = NodePointer.builder()
                    .name("dead_unprunable").name(node.getBackingNode().getString()).build();
            return List.of(livingPointer, deadPointer);
        }

        return EditorSavegameAdapter.super.createNodeJumps(state, node);
    }

    @Override
    public NodePointer createNodeJump(EditorState state, EditorRealNode node) {
        if (!state.isSavegame()) {
            return null;
        }

        var keyOpt = node.getKeyName();
        var parentKey = Optional.ofNullable(node.getParent())
                .flatMap(p -> p.getKeyName());

        // Character triggered events
        if (keyOpt.isPresent() && parentKey.isPresent() && node.getBackingNode().isValue() && keyOpt.get().equals("identity")) {
            var parent = node.getParent();
            boolean isTypeChar = parent.isReal() && ((EditorRealNode) parent).getBackingNode().getNodeForKeyIfExistent("type")
                    .map(t -> t.isValue() && t.getString().equals("char")).orElse(false);
            if (isTypeChar) {
                return NodePointer.builder().name("living").name(node.getBackingNode().getString()).build();
            }
        }

        if (keyOpt.isPresent() && node.getBackingNode().isValue()) {
            return get(keyOpt.get(), node.getBackingNode().getString());
        }

        if (keyOpt.isPresent() && node.getBackingNode().isArray() && node.getBackingNode().getArrayNode().size() == 1
                && node.getBackingNode().getNodeArray().getFirst().isValue()) {
            return get(keyOpt.get(), node.getBackingNode().getNodeArray().getFirst().getString());
        }

        if (parentKey.isPresent() && node.getBackingNode().isValue()) {
            return get(parentKey.get(), node.getBackingNode().getString());
        }
        
        return null;
    }

    @Override
    public javafx.scene.Node createNodeTag(EditorState state, EditorRealNode node, Region valueDisplay) {
        return FACTORIES.stream()
                .filter(fac -> fac.checkIfApplicable(state, node))
                .findFirst().map(fac -> fac.create(state, node, valueDisplay))
                .orElse(null);
    }
}
