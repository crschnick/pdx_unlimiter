package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.lang.LanguageManager;
import com.crschnick.pdx_unlimiter.app.lang.LocalisationHelper;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public final class PdxuI18n {

    private static final Map<String, String> defaultMap = new HashMap<>();
    private static final Map<String, String> map = new HashMap<>();

    public static void initDefault() {
        try {
            Files.list(PdxuInstallation.getInstance().getLanguageLocation()).forEach(p -> {
                if (LocalisationHelper.isLanguage(p, LanguageManager.DEFAULT)) {
                    defaultMap.putAll(LocalisationHelper.loadTranslations(p));
                }
            });
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void init() {
        try {
            Files.list(PdxuInstallation.getInstance().getLanguageLocation()).forEach(p -> {
                if (LocalisationHelper.isLanguage(p, LanguageManager.getInstance().getActiveLanguage())) {
                    map.putAll(LocalisationHelper.loadTranslations(p));
                }
            });
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void reset() {
        map.clear();
    }

    public static String get(String s, String... vars) {
        return getLocalised(s, vars);
    }

    private static String getLocalised(String s, String... vars) {
        var localisedString = map.get(s);
        if (localisedString == null) {
            LoggerFactory.getLogger(PdxuI18n.class).warn("No localisation found for key " + s);
            var def = defaultMap.get(s);
            return def != null ? LocalisationHelper.getValue(def, vars) : s;
        }

        return LocalisationHelper.getValue(localisedString, vars);
    }
}
