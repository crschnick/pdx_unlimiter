package com.crschnick.pdx_unlimiter.app.lang;

public class Language {

    public static Language ENGLISH = new Language("l_english", "English");

    private final String id;
    private final String displayName;

    public Language(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
