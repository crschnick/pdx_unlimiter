package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.app.core.settings.AbstractSettings;
import com.crschnick.pdxu.app.core.settings.SettingsEntry;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;
import org.apache.commons.lang3.SystemUtils;

import java.util.LinkedHashMap;

public class EditorSettings extends AbstractSettings {

    private static BidiMap<String, String> indentationTypes() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("1 " + PdxuI18n.get("SPACE"), " ");
        m.put("2 " + PdxuI18n.get("SPACES"), "  ");
        m.put("4 " + PdxuI18n.get("SPACES"), "    ");
        m.put("1 " + PdxuI18n.get("TAB"), "\t");
        m.put("2 " + PdxuI18n.get("TABS"), "\t\t");
        return new DualLinkedHashBidiMap<>(m);
    };

    private static BidiMap<String, String> indentationIdentity() {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put(" ", " ");
        m.put("  ", "  ");
        m.put("    ", "    ");
        m.put("\t", "\t");
        m.put("\t\t", "\t\t");
        return new DualLinkedHashBidiMap<>(m);
    };

    public final SettingsEntry.ChoiceEntry<String> indentation = new SettingsEntry.ChoiceEntry<>(
            "INDENTATION",
            "indentation",
            "\t",
            indentationIdentity(),
            t -> indentationTypes().inverseBidiMap().get(t)
    );
    public final SettingsEntry.IntegerEntry pageSize = new SettingsEntry.IntegerEntry(
            "PAGE_SIZE",
            "pageSize",
            200,
            50,
            1000
    );
    public final SettingsEntry.IntegerEntry maxTooltipLines = new SettingsEntry.IntegerEntry(
            "MAX_TOOLTIP_LINES",
            "maxTooltipLines",
            10,
            5,
            30
    );
    public final SettingsEntry.BooleanEntry enableNodeTags = new SettingsEntry.BooleanEntry(
            "ENABLE_NODE_TAGS",
            "enableNodeTags",
            true
    );
    public final SettingsEntry.BooleanEntry enableNodeJumps = new SettingsEntry.BooleanEntry(
            "ENABLE_NODE_JUMPS",
            "enableNodeJumps",
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
                    System.getenv("VISUAL") : null)
    );
    public final SettingsEntry.IntegerEntry externalEditorWaitInterval = new SettingsEntry.IntegerEntry(
            "EXTERNAL_EDITOR_WAIT_INTERVAL",
            "externalEditorWaitInterval",
            500,
            0,
            3000
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
