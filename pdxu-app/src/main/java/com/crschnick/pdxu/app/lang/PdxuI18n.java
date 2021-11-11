package com.crschnick.pdxu.app.lang;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
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
                    map.clear();
                    map.putAll(LocalisationHelper.loadTranslations(p));
                }
            });
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void reset() {
        // Don't clear language map when resetting to retain translation
        // for error messages that are shown between reset() and init()
        // map.clear();
    }

    public static String get(String s, String... vars) {
        return getLocalised(s, vars);
    }

    private static String getLocalised(String s, String... vars) {
        if (LanguageManager.getInstance().getActiveLanguage() == Language.TRANSLATION_HELPER) {
            return "#" + s;
        }

        var localisedString = map.get(s);
        if (localisedString == null) {
            LoggerFactory.getLogger(PdxuI18n.class).trace("No localisation found for key " + s);
            var def = defaultMap.get(s);
            return def != null ? LocalisationHelper.getValue(def, vars) : s;
        }

        return LocalisationHelper.getValue(localisedString, vars);
    }
}
