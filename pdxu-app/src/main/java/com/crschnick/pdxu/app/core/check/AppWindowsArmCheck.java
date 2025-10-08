package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.OsType;

public class AppWindowsArmCheck {

    public static void check() {
        if (OsType.ofLocal() != OsType.WINDOWS) {
            return;
        }

        if (!AppProperties.get().getArch().equals("x86_64")) {
            return;
        }

        var armProgramFiles = System.getenv("ProgramFiles(Arm)");
        if (armProgramFiles != null) {
            ErrorEventFactory.fromMessage("You are running the x86-64 version on an ARM system."
                            + " There is a native build available that comes with much better performance."
                            + " Please install that one instead.")
                    .expected()
                    .handle();
        }
    }
}
