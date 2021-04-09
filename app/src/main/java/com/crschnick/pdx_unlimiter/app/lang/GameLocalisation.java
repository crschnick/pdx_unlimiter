package com.crschnick.pdx_unlimiter.app.lang;

import com.crschnick.pdx_unlimiter.app.core.CacheManager;
import com.crschnick.pdx_unlimiter.app.installation.GameFileContext;
import com.crschnick.pdx_unlimiter.app.util.CascadeDirectoryHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GameLocalisation {

    public static class LocalisationCache extends CacheManager.Cache {

        private final Map<String, String> strings = new HashMap<>();

        public LocalisationCache() {
            super(CacheManager.Scope.SAVEGAME_CAMPAIGN_SPECIFIC);
        }

        public void loadLocalisations(Language lang, GameFileContext ctx) {
            CascadeDirectoryHelper.traverseDirectory(
                    Path.of("localisation"),
                    ctx,
                    file -> {
                        if (!LocalisationHelper.isLanguage(file, lang)) {
                            return;
                        }

                        var loc = LocalisationHelper.loadTranslations(file);
                        strings.putAll(loc);
                    });
        }

        public boolean isLoaded() {
            return strings.size() > 0;
        }
    }

    public static String getLocalisedValue(String key, SavegameInfo<?> info) {
        var cache = CacheManager.getInstance().get(LocalisationCache.class);
        if (!cache.isLoaded()) {
            cache.loadLocalisations(
                    LanguageManager.getInstance().getActiveLanguage(),
                    GameFileContext.fromInfo(info));
        }

        return cache.strings.getOrDefault(key, "Unknown");
    }
}