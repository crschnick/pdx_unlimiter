package com.crschnick.pdxu.app.lang;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.util.JsonHelper;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageManager {

    public static final Language DEFAULT = Language.ENGLISH;
    public static final Language TRANSLATION_HELPER = Language.TRANSLATION_HELPER;

    private static LanguageManager INSTANCE;
    private final BidiMap<String, Language> languages = new DualHashBidiMap<>();
    private static final Logger logger = LoggerFactory.getLogger(LanguageManager.class);

    public static void init() {
        INSTANCE = new LanguageManager();
        INSTANCE.load();
    }

    public static LanguageManager getInstance() {
        return INSTANCE;
    }

    private void load() {
        try {
            logger.debug("Loading languages ...");
            var n = JsonHelper.read(PdxuInstallation.getInstance().getLanguageLocation().resolve("languages.json"));
            n.get("languages").properties().forEach(e -> {
                var loc = LocaleUtils.toLocale(e.getValue().textValue());
                languages.put(e.getKey(), new Language(loc, e.getKey(), loc.getDisplayName(loc)));
                logger.debug("Loaded " + loc.getDisplayName(loc));
            });
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }

        languages.put(DEFAULT.getId(), DEFAULT);
        languages.put(TRANSLATION_HELPER.getId(), TRANSLATION_HELPER);
    }

    public Language getActiveLanguage() {
        var current = Settings.getInstance().language.getValue();
        if (SavegameManagerState.get().current() == null) {
            return current;
        }

        if (!Settings.getInstance().useGameLanguage.getValue()) {
            return current;
        }

        var l = GameInstallation.ALL.get(SavegameManagerState.get().current()).getLanguage();
        return l != null ? l : current;
    }

    public BidiMap<String, Language> getLanguages() {
        return languages;
    }

    public Language byId(String langId) {
        return languages.get(langId);
    }
}
