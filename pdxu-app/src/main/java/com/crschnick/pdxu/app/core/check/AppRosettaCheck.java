package com.crschnick.pdxu.app.core.check;

import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;

public class AppRosettaCheck {

    public static void check() {
        if (OsType.ofLocal() != OsType.MACOS) {
            return;
        }

        if (!AppProperties.get().getArch().equals("x86_64")) {
            return;
        }

        var ret = LocalExec.readStdoutIfPossible("sysctl -n sysctl.proc_translated");
        if (ret.isEmpty()) {
            return;
        }

        if (ret.get().equals("1")) {
            ErrorEventFactory.fromMessage("You are running the Intel version on an Apple Silicon system."
                            + " There is a native build available that comes with much better performance."
                            + " Please install that one instead.")
                    .expected()
                    .handle();
        }
    }
}
