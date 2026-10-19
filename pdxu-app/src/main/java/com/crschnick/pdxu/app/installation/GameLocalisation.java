package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GameLocalisation {

    public static String getLocalisedValue(String key, SavegameInfo<?> info) {
        return getLocalisedValue(key, GameFileContext.fromData(info.getData()));
    }

    public static String getLocalisedValue(String key, SavegameData<?> sgData) {
        return getLocalisedValue(key, GameFileContext.fromData(sgData));
    }

    public static String getLocalisedValue(String key, GameFileContext ctx) {
        var cache = GameCacheManager.getInstance().get(LocalisationCache.class);
        if (!cache.isLoaded()) {
            cache.loadLocalisations(
                    GameLanguage.bySupportedLocale(AppPrefs.get().language().getValue()), ctx);
        }

        var value = cache.strings.getOrDefault(key, key);
        for (int i = 0; i < 5; i++) {
            var reference = REFERENCE_PATTERN.matcher(value);
            if (!reference.matches()) {
                break;
            }

            if (reference.group(3) != null) {
                value = reference.group(3);
                continue;
            }

            var referencedKey = reference.group(1) != null ? reference.group(1) : reference.group(2);
            if (!cache.strings.containsKey(referencedKey)) {
                break;
            }
            value = cache.strings.get(referencedKey);
        }
        return value;
    }

    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^(?:\\$([^$\\s]+)\\$|\\[([A-Za-z0-9_]+)\\]|\\[Concept\\('[^']*',\\s*'([^']*)'\\)\\])$");

    public static class LocalisationCache extends GameCacheManager.Cache {

        private final Map<String, String> strings = new HashMap<>();

        public LocalisationCache() {
            super(GameCacheManager.Scope.GAME_SPECIFIC);
        }

        public void loadLocalisations(GameLanguage lang, GameFileContext ctx) {
            var dir = ctx.getInstall()
                    .getInstallDir()
                    .resolve(ctx.getInstall().getType().getLocalisationsDirectory());
            CascadeDirectoryHelper.traverseDirectory(dir, ctx, file -> {
                if (!GameLocalisationHelper.isLanguage(file, lang)) {
                    return;
                }

                var loc = GameLocalisationHelper.loadTranslations(file);
                strings.putAll(loc);
            });
        }

        public boolean isLoaded() {
            return strings.size() > 0;
        }
    }
}
