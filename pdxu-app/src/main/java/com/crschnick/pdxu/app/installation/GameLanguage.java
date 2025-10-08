package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.prefs.SupportedLocale;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum GameLanguage {

    ENGLISH("l_english", SupportedLocale.ENGLISH),
    GERMAN("l_german", SupportedLocale.GERMAN),
    FRENCH("l_french", SupportedLocale.FRENCH),
    SPANISH("l_spanish", SupportedLocale.SPANISH),
    POLISH("l_polish", SupportedLocale.POLISH),
    CHINESE("l_chinese", SupportedLocale.CHINESE_SIMPLIFIED),
    JAPANESE("l_japanese", SupportedLocale.JAPANESE),
    RUSSIAN("l_russian", SupportedLocale.RUSSIAN),
    PORTUGUESE("l_portuguese", SupportedLocale.PORTUGUESE);

    public static GameLanguage byId(String langId) {
        return Arrays.stream(values()).filter(gameLanguage -> gameLanguage.getId().equals(langId)).findFirst().orElse(ENGLISH);
    }

    public static GameLanguage bySupportedLocale(SupportedLocale supportedLocale) {
        return Arrays.stream(values()).filter(gameLanguage -> gameLanguage.getSupportedLocale().equals(supportedLocale)).findFirst().orElse(ENGLISH);
    }

    String id;
    SupportedLocale supportedLocale;

    @Override
    public String toString() {
        return id;
    }
}
