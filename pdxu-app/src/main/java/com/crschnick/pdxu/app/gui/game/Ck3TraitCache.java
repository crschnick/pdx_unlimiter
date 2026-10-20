package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import javafx.scene.image.Image;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Ck3TraitCache extends GameCacheManager.Cache {

    private static final Path TRAITS_DIR = Path.of("gfx", "interface", "icons", "traits");

    public static final List<String> SKILLS =
            List.of("diplomacy", "martial", "stewardship", "intrigue", "learning", "prowess");

    private record Threshold(double xp, int[] skills) {}

    private record Trait(String icon, List<String> names, int[] skills, List<List<Threshold>> tracks) {}

    public record SkillSource(String nameKey, int[] skills) {}

    public static List<SkillSource> getTraitSkillSources(
            List<String> traits, List<Double> xp, boolean female, GameFileContext ctx) {
        var definitions = getTraits(ctx);
        var trackCount = traits.stream()
                .map(definitions::get)
                .mapToInt(t -> t != null ? t.tracks().size() : 0)
                .sum();
        var applyTracks = trackCount == xp.size();

        var sources = new ArrayList<SkillSource>();
        var xpIndex = 0;
        for (var trait : traits) {
            var definition = definitions.get(trait);
            if (definition == null) {
                continue;
            }

            var skills = definition.skills().clone();
            if (applyTracks) {
                for (var track : definition.tracks()) {
                    var value = xp.get(xpIndex++);
                    track.stream()
                            .filter(threshold -> threshold.xp() <= value)
                            .reduce((first, second) -> second)
                            .ifPresent(threshold -> add(skills, threshold.skills()));
                }
            }
            sources.add(new SkillSource(traitNameKey(trait, female, ctx), skills));
        }
        return sources;
    }

    static void add(int[] target, int[] values) {
        for (int i = 0; i < target.length; i++) {
            target[i] += values[i];
        }
    }

    static int[] getSkills(Node node, Map<String, Double> scriptValues) {
        var skills = new int[SKILLS.size()];
        for (int i = 0; i < SKILLS.size(); i++) {
            skills[i] = node.getNodeForKeyIfExistent(SKILLS.get(i))
                    .filter(Node::isValue)
                    .map(n -> getValue(n.getString(), scriptValues))
                    .orElse(0);
        }
        return skills;
    }

    private static int getValue(String value, Map<String, Double> scriptValues) {
        try {
            return (int) Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return scriptValues.getOrDefault(value, 0.0).intValue();
        }
    }

    private static List<Threshold> getTrack(Node track, Map<String, Double> scriptValues) {
        var thresholds = new ArrayList<Threshold>();
        track.forEach((key, modifiers) -> {
            if (key == null || !modifiers.isArray()) {
                return;
            }

            Double xp;
            try {
                xp = Double.parseDouble(key);
            } catch (NumberFormatException e) {
                xp = scriptValues.get(key);
            }
            if (xp != null) {
                thresholds.add(new Threshold(xp, getSkills(modifiers, scriptValues)));
            }
        });
        thresholds.sort(Comparator.comparingDouble(Threshold::xp));
        return thresholds;
    }

    static synchronized Map<String, Double> getScriptValues(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Ck3TraitCache.class);
        if (cache.scriptValues != null) {
            return cache.scriptValues;
        }

        var map = new HashMap<String, Double>();
        CascadeDirectoryHelper.traverseDirectory(Path.of("common").resolve("script_values"), ctx, path -> {
            try {
                TextFormatParser.ck3().parse(path).forEach((key, value) -> {
                    if (key == null || !value.isValue()) {
                        return;
                    }

                    try {
                        map.putIfAbsent(key, Double.parseDouble(value.getString()));
                    } catch (NumberFormatException ignored) {
                    }
                });
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).omit().handle();
            }
        });
        cache.scriptValues = map;
        return map;
    }

    public static String traitNameKey(String trait, boolean female, GameFileContext ctx) {
        var definition = getTraits(ctx).get(trait);
        if (definition == null || definition.names().isEmpty()) {
            return "trait_" + trait;
        }

        var otherGender = female ? "_male" : "_female";
        var names = definition.names().stream()
                .filter(n -> !n.contains(otherGender))
                .toList();
        if (names.isEmpty()) {
            names = definition.names();
        }
        return names.stream()
                .filter(n -> getLevel(n) != -1)
                .min(Comparator.comparingInt(Ck3TraitCache::getLevel))
                .orElse(names.getFirst());
    }

    private static int getLevel(String name) {
        var index = name.lastIndexOf('_');
        if (index == -1) {
            return -1;
        }

        try {
            return Integer.parseInt(name.substring(index + 1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static Image traitImage(String trait, GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Ck3TraitCache.class);
        return cache.images.computeIfAbsent(trait, t -> {
            var definition = getTraits(ctx).get(t);
            var file = definition != null && definition.icon() != null ? definition.icon() : t + ".dds";
            return CascadeDirectoryHelper.openFile(TRAITS_DIR.resolve(file), ctx)
                    .map(ImageHelper::loadImage)
                    .orElse(ImageHelper.DEFAULT_IMAGE);
        });
    }

    private static synchronized Map<String, Trait> getTraits(GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(Ck3TraitCache.class);
        if (cache.traits != null) {
            return cache.traits;
        }

        var map = new HashMap<String, Trait>();
        var scriptValues = getScriptValues(ctx);
        CascadeDirectoryHelper.traverseDirectory(Path.of("common").resolve("traits"), ctx, path -> {
            try {
                TextFormatParser.ck3().parse(path).forEach((trait, node) -> {
                    if (trait == null || !node.isArray()) {
                        return;
                    }

                    var icons = new ArrayList<String>();
                    node.getNodeForKeyIfExistent("icon").ifPresent(n -> collectNames(n, icons));
                    var icon = !icons.isEmpty() ? icons.getFirst() : null;
                    var names = new ArrayList<String>();
                    node.getNodeForKeyIfExistent("name").ifPresent(n -> collectNames(n, names));
                    var tracks = new ArrayList<List<Threshold>>();
                    node.getNodeForKeyIfExistent("track")
                            .filter(Node::isArray)
                            .ifPresent(track -> tracks.add(getTrack(track, scriptValues)));
                    node.getNodeForKeyIfExistent("tracks").filter(Node::isArray).ifPresent(all -> all.forEach(
                            (name, track) -> {
                                if (name != null && track.isArray()) {
                                    tracks.add(getTrack(track, scriptValues));
                                }
                            }));
                    map.put(trait, new Trait(icon, names, getSkills(node, scriptValues), tracks));
                });
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).omit().handle();
            }
        });
        cache.traits = map;
        return map;
    }

    private static void collectNames(Node node, List<String> names) {
        if (node.isValue()) {
            names.add(node.getString());
            return;
        }
        if (!node.isArray()) {
            return;
        }

        node.forEach((k, v) -> {
            if ("desc".equals(k) && v.isValue()) {
                names.add(v.getString());
            } else if (v.isArray()) {
                collectNames(v, names);
            }
        });
    }

    private final Map<String, Image> images = new ConcurrentHashMap<>();
    private Map<String, Trait> traits;
    private Map<String, Double> scriptValues;

    public Ck3TraitCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
