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

    private record Trait(String icon, List<String> names) {}

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
                    map.put(trait, new Trait(icon, names));
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

    public Ck3TraitCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }
}
