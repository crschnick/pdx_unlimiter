package com.crschnick.pdxu.app.lang;

import com.crschnick.pdxu.app.core.CacheManager;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GameLocalisation {

    public static String getLocalisedValue(String key, SavegameInfo<?> info) {
        if (LanguageManager.getInstance().getActiveLanguage() == Language.TRANSLATION_HELPER) {
            return key;
        }

        var cache = CacheManager.getInstance().get(LocalisationCache.class);
        if (!cache.isLoaded()) {
            cache.loadLocalisations(
                    LanguageManager.getInstance().getActiveLanguage(),
                    GameFileContext.fromInfo(info));
        }

        return cache.strings.getOrDefault(key, "Unknown");
    }

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
}
