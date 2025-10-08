package com.crschnick.pdxu.app.prefs;

public interface PrefsValue {

    default boolean isSelectable() {
        return true;
    }
}
