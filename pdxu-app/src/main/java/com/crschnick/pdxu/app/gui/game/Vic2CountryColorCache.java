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

public class Vic2CountryColorCache extends GameCacheManager.Cache {

    private final Map<String, Optional<GameColor>> colors = new HashMap<>();
    private Map<String, String> countryFiles;

    public Vic2CountryColorCache() {
        super(GameCacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
    }

    public static synchronized Optional<GameColor> getColor(GameFileContext ctx, String tag) {
        var cache = GameCacheManager.getInstance().get(Vic2CountryColorCache.class);
        if (cache.countryFiles == null) {
            cache.countryFiles = loadCountryFiles(ctx);
        }

        return cache.colors.computeIfAbsent(tag, t -> {
            var file = cache.countryFiles.get(t);
            if (file == null) {
                return Optional.empty();
            }

            return CascadeDirectoryHelper.openFile(Path.of("common").resolve(file), ctx)
                    .flatMap(path -> {
                        try {
                            return TextFormatParser.vic2()
                                    .parse(path)
                                    .getNodeForKeyIfExistent("color")
                                    .filter(Node::isArray)
                                    .map(GameColor::fromRgbArray);
                        } catch (Exception e) {
                            return Optional.empty();
                        }
                    });
        });
    }

    private static Map<String, String> loadCountryFiles(GameFileContext ctx) {
        var map = new HashMap<String, String>();
        CascadeDirectoryHelper.openFile(Path.of("common", "countries.txt"), ctx).ifPresent(path -> {
            try {
                TextFormatParser.vic2().parse(path).forEach((k, v) -> {
                    if (k != null && k.length() == 3 && v.isValue()) {
                        map.put(k, v.getString());
                    }
                });
            } catch (Exception ignored) {
            }
        });
        return map;
    }
}
