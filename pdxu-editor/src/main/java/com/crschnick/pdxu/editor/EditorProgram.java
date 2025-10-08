package com.crschnick.pdxu.editor;

import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class EditorProgram {

    public static String getDefaultEditor() {
        if (SystemUtils.IS_OS_WINDOWS) {
            var npp = Path.of("C:\\Program Files\\Notepad++\\notepad++.exe");
            if (Files.exists(npp)) {
                return npp.toString();
            }

            var np = Path.of("C:\\Windows\\System32\\notepad.exe");
            if (Files.exists(np)) {
                return np.toString();
            }

            return "notepad";
        }

        if (SystemUtils.IS_OS_MAC) {
            return "TextEdit.app";
        }

        return System.getenv("VISUAL") != null ?
                System.getenv("VISUAL") : null;
    }
}
