package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class PdxuI18n {

    private static final Map<LocalisationHelper.Language, PdxuI18n> ALL = new HashMap<>();

    private Map<String, String> map = new HashMap<>();


    public static String get(String s, String... vars) {
        return get(LocalisationHelper.Language.ENGLISH).getLocalised(s, vars);
    }

    public static PdxuI18n get(LocalisationHelper.Language language) {
        if (ALL.containsKey(language)) {
            return ALL.get(language);
        }

        var i18n = new PdxuI18n();
        i18n.map = LocalisationHelper.loadTranslations(PdxuInstallation.getInstance().getLanguageLocation()
                .resolve("settings.yml"), language);
        i18n.map.putAll(LocalisationHelper.loadTranslations(PdxuInstallation.getInstance().getLanguageLocation()
                .resolve("install.yml"), language));
        i18n.map.putAll(LocalisationHelper.loadTranslations(PdxuInstallation.getInstance().getLanguageLocation()
                .resolve("launcher.yml"), language));

        ALL.put(language, i18n);
        return i18n;
    }

    public String getLocalised(String s, String... vars) {
        var localisedString = getMap().get(s);
        if (localisedString == null) {
            LoggerFactory.getLogger(PdxuI18n.class).error("No localisation found for key " + s);
            return s;
        }

        return LocalisationHelper.getValue(localisedString, vars);
    }

    public Map<String, String> getMap() {
        return map;
    }
}
