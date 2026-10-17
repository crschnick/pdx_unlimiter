package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.core.AppInstallation;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;

public class AppSystemFontCheck {

    public static void init() {
        if (OsType.ofLocal() != OsType.LINUX) {
            return;
        }

        if (hasFonts()) {
            return;
        }

        System.setProperty(
                "prism.fontdir",
                AppInstallation.ofCurrent()
                        .getBaseInstallationPath()
                        .resolve("fonts")
                        .toString());
        System.setProperty("prism.embeddedfonts", "true");
    }

    private static boolean hasFonts() {
        var out = LocalExec.readStdoutIfPossible("fc-match");
        return out.isPresent() && !out.get().isBlank();
    }
}
