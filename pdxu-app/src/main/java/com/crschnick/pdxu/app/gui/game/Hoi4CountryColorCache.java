package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Hoi4CountryColorCache extends GameCacheManager.Cache {

    private static final Set<String> COLOR_FILES = Set.of("colors.txt", "cosmetic.txt");

    private Map<String, GameColor> colors;

    public Hoi4CountryColorCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }

    public static synchronized Optional<GameColor> getColor(GameFileContext ctx, String tag) {
        var cache = GameCacheManager.getInstance().get(Hoi4CountryColorCache.class);
        if (cache.colors == null) {
            cache.colors = loadColors(ctx);
        }
        return Optional.ofNullable(cache.colors.get(tag));
    }

    private static Map<String, GameColor> loadColors(GameFileContext ctx) {
        var map = new HashMap<String, GameColor>();
        CascadeDirectoryHelper.traverseDirectory(Path.of("common", "countries"), ctx, file -> {
            if (!COLOR_FILES.contains(file.getFileName().toString())) {
                return;
            }

            try {
                var content = TextFormatParser.hoi4().parse(file);
                content.forEach((k, v) -> {
                    if (k == null || !v.isArray()) {
                        return;
                    }

                    v.getNodeForKeyIfExistent("color").ifPresent(c -> {
                        try {
                            map.put(k, GameColor.fromColorNode(c));
                        } catch (IllegalArgumentException ignored) {
                        }
                    });
                });
            } catch (Exception ignored) {
            }
        });
        return map;
    }
}
