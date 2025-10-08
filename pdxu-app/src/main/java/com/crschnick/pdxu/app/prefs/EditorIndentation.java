package com.crschnick.pdxu.app.prefs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EditorIndentation implements PrefsChoiceValue {

    ONE_SPACE("oneSpace", " "),
    TWO_SPACES("twoSpaces", "  "),
    FOUR_SPACES("fourSpaces", "  "),
    ONE_TAB("oneTab", "\t"),
    TWO_TABS("twoTabs", "\t\t");

    private final String id;
    private final String value;
}
