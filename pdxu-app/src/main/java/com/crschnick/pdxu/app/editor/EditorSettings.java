package com.crschnick.pdxu.app.editor;

import com.crschnick.pdxu.app.core.settings.AbstractSettings;
import com.crschnick.pdxu.app.core.settings.SettingsEntry;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.SystemUtils;

import java.util.Map;

public class EditorSettings extends AbstractSettings {

    private static final BidiMap<String, String> INDENTATION_TYPES = new DualHashBidiMap<>(Map.of(
            "1 space", " ",
            "2 spaces", "  ",
            "4 spaces", "    ",
            "1 tab", "\t",
            "2 tabs", "\t\t"));

    public final SettingsEntry.ChoiceEntry<String> indentation = new SettingsEntry.ChoiceEntry<>(
            "IDENTATION",
            "indentation",
            "\t",
            INDENTATION_TYPES.inverseBidiMap(),
            t -> INDENTATION_TYPES.inverseBidiMap().get(t)
    );
    public final SettingsEntry.IntegerEntry navHistorySize = new SettingsEntry.IntegerEntry(
            "NAV_HISTORY_SIZE",
            "navHistorySize",
            25,
            5,
            100
    );
    public final SettingsEntry.IntegerEntry pageSize = new SettingsEntry.IntegerEntry(
            "PAGE_SIZE",
            "pageSize",
            200,
            50,
            1000
    );
    public final SettingsEntry.IntegerEntry maxTooltipWidth = new SettingsEntry.IntegerEntry(
            "MAX_TOOLTIP_SIZE",
            "maxTooltipWidth",
            800,
            100,
            2000
    );
    public final SettingsEntry.IntegerEntry maxTooltipLines = new SettingsEntry.IntegerEntry(
            "MAX_TOOLTIP_LINES",
            "maxTooltipLines",
            10,
            5,
            30
    );
    public final SettingsEntry.BooleanEntry enabledNodeTags = new SettingsEntry.BooleanEntry(
            "ENABLE_NODE_TAGS",
            "enabledNodeTags",
            true
    );
    public final SettingsEntry.BooleanEntry enabledNodeJumps = new SettingsEntry.BooleanEntry(
            "ENABLE_NODE_JUMPS",
            "enabledNodeJumps",
            true
    );
    public final SettingsEntry.BooleanEntry warnOnTypeChange = new SettingsEntry.BooleanEntry(
            "WARN_ON_TYPE_CHANGE",
            "warnOnTypeChange",
            true
    );
    public final SettingsEntry.ProgramEntry externalEditor = new SettingsEntry.ProgramEntry(
            "EXTERNAL_EDITOR",
            "externalEditor",
            SystemUtils.IS_OS_WINDOWS ? "notepad" : (System.getenv("VISUAL") != null ?
                    System.getenv("VISUAL") : System.getenv("EDITOR"))
    );

    private static EditorSettings INSTANCE;

    public static void init() {
        INSTANCE = new EditorSettings();
        INSTANCE.load();
    }

    public static EditorSettings getInstance() {
        return INSTANCE;
    }


    @Override
    protected String createName() {
        return "editor";
    }

    @Override
    public void check() {

    }
}
