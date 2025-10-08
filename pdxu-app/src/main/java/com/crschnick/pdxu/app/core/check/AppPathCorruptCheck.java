package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;

public class AppPathCorruptCheck {

    public static void check() {
        if (OsType.ofLocal() != OsType.WINDOWS) {
            return;
        }

        var where = LocalExec.readStdoutIfPossible("where", "powershell");
        if (where.isPresent()) {
            return;
        }

        ErrorEventFactory.fromMessage(
                        "Your system PATH looks to be corrupt, essential system tools are not available. This will cause the application to not function "
                                + "correctly. Please fix your PATH environment variable to include the base Windows tool directories like C:\\Windows\\system32 and others.")
                .expected()
                .handle();
    }
}
