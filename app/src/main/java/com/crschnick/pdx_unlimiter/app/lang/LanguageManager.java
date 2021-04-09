package com.crschnick.pdx_unlimiter.app.lang;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

public class LanguageManager {

    public static final Language DEFAULT = Language.ENGLISH;

    private static LanguageManager INSTANCE;

    public static void init() {
        INSTANCE = new LanguageManager();
        INSTANCE.load();
    }

    public static LanguageManager getInstance() {
        return INSTANCE;
    }

    private final BidiMap<String,Language> languages = new DualHashBidiMap<>();

    private void load() {
        try {
            var n = JsonHelper.read(PdxuInstallation.getInstance().getLanguageLocation().resolve("languages.json"));
            n.get("languages").fields().forEachRemaining(e -> {
                languages.put(e.getKey(), new Language(e.getKey(), e.getValue().textValue()));
            });
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }

        languages.put(DEFAULT.getId(), DEFAULT);
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
