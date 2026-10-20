package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Ck3SkillModifierCache extends GameCacheManager.Cache {

    private Map<String, int[]> modifiers;
    private Map<String, int[]> perks;

    public Ck3SkillModifierCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }

    public static synchronized List<Ck3TraitCache.SkillSource> getModifierSkillSources(
            List<String> modifiers, List<String> perks, GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Ck3SkillModifierCache.class);
        if (cache.modifiers == null) {
            var scriptValues = Ck3TraitCache.getScriptValues(ctx);
            cache.modifiers = load(ctx, "modifiers", node -> Ck3TraitCache.getSkills(node, scriptValues));
            cache.perks = load(ctx, "lifestyle_perks", node -> {
                var skills = new int[Ck3TraitCache.SKILLS.size()];
                for (var modifier : node.getNodesForKey("character_modifier")) {
                    if (modifier.isArray()) {
                        Ck3TraitCache.add(skills, Ck3TraitCache.getSkills(modifier, scriptValues));
                    }
                }
                return skills;
            });
        }

        var sources = new ArrayList<Ck3TraitCache.SkillSource>();
        for (var modifier : modifiers) {
            var skills = cache.modifiers.get(modifier);
            if (skills != null) {
                sources.add(new Ck3TraitCache.SkillSource(modifier, skills));
            }
        }
        for (var perk : perks) {
            var skills = cache.perks.get(perk);
            if (skills != null) {
                sources.add(new Ck3TraitCache.SkillSource(perk + "_name", skills));
            }
        }
        return sources;
    }

    private static Map<String, int[]> load(
            GameFileContext ctx, String directory, Function<Node, int[]> skills) {
        var map = new HashMap<String, int[]>();
        CascadeDirectoryHelper.traverseDirectory(Path.of("common").resolve(directory), ctx, path -> {
            try {
                TextFormatParser.ck3().parse(path).forEach((key, node) -> {
                    if (key != null && node.isArray()) {
                        map.put(key, skills.apply(node));
                    }
                });
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).omit().handle();
            }
        });
        return map;
    }
}
