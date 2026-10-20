package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.installation.GameCacheManager;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.model.GameColor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class Ck2TitleColorCache extends GameCacheManager.Cache {

    private static final Pattern TITLE = Pattern.compile("[ekdcb]_.+");

    private Map<String, GameColor> colors;

    public Ck2TitleColorCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }

    public static synchronized Optional<GameColor> getColor(GameFileContext ctx, String title) {
        var cache = GameCacheManager.getInstance().get(Ck2TitleColorCache.class);
        if (cache.colors == null) {
            var map = new HashMap<String, GameColor>();
            CascadeDirectoryHelper.traverseDirectory(Path.of("common", "landed_titles"), ctx, file -> {
                if (!file.getFileName().toString().endsWith(".txt")) {
                    return;
                }

                try {
                    collect(TextFormatParser.ck2().parse(file), map);
                } catch (Exception ignored) {
                }
            });
            cache.colors = map;
        }
        return Optional.ofNullable(cache.colors.get(title));
    }

    private static void collect(Node node, Map<String, GameColor> map) {
        node.forEach((k, v) -> {
            if (k == null || !v.isArray() || !TITLE.matcher(k).matches()) {
                return;
            }

            v.getNodeForKeyIfExistent("color")
                    .filter(c -> c.isArray() && c.getNodeArray().size() == 3)
                    .ifPresent(c -> map.putIfAbsent(k, GameColor.fromRgbArray(c)));
            collect(v, map);
        });
    }
}
