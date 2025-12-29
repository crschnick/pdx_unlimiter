package com.crschnick.pdxu.editor;

import com.crschnick.pdxu.app.core.AppSystemInfo;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class EditorProgram {

    public static String getDefaultEditor() {
        if (SystemUtils.IS_OS_WINDOWS) {
            var vsCodeUser = AppSystemInfo.ofWindows().getLocalAppData().resolve("Programs", "Microsoft VS Code", "Code.exe");
            if (Files.exists(vsCodeUser)) {
                return vsCodeUser.toString();
            }

            var vsCodeSystem = AppSystemInfo.ofWindows().getProgramFiles().resolve("Microsoft VS Code", "Code.exe");
            if (Files.exists(vsCodeSystem)) {
                return vsCodeSystem.toString();
            }

            var npp = AppSystemInfo.ofWindows().getProgramFiles().resolve("Notepad++", "notepad++.exe");
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

        return System.getenv("VISUAL") != null ? System.getenv("VISUAL") : null;
    }
}
