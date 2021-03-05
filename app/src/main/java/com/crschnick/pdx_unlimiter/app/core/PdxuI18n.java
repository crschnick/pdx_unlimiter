package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PdxuI18n {

    private static final Map<LocalisationHelper.Language, PdxuI18n> ALL = new HashMap<>();

    private Map<String, String> map = new HashMap<>();

    public static String get(String s, String... vars) {
        return get(LocalisationHelper.Language.ENGLISH).getValue(s, vars);
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

    private static final String VAR_PATTERN = "\\$\\w+\\$";

    public String getValue(String s, String... vars) {
        var val = map.get(s);
        for (var v : vars) {
            val = val.replaceAll(VAR_PATTERN, v);
        }
        return val;
    }
}
