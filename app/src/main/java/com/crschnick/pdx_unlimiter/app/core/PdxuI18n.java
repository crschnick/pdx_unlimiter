package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PdxuI18n {

    private static final Map<LocalisationHelper.Language, PdxuI18n> ALL = new HashMap<>();

    private Map<String, String> map = new HashMap<>();

    public static String get(String s) {
        return get(LocalisationHelper.Language.ENGLISH).getValue(s);
    }

    public static PdxuI18n get(LocalisationHelper.Language language) {
        if (ALL.containsKey(language)) {
            return ALL.get(language);
        }

        var i18n = new PdxuI18n();
        i18n.map = LocalisationHelper.loadTranslations(PdxuInstallation.getInstance().getLanguageLocation()
                .resolve("settings.yml"), language);

        ALL.put(language, i18n);
        return i18n;
    }

    public String getValue(String s) {
        return map.get(s);
    }
}
