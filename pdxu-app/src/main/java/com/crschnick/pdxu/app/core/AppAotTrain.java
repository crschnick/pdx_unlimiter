package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.ThreadHelper;

public class AppAotTrain {

    public static void runTrainingMode() throws Throwable {
        // Linux runners don't support graphics
        if (OsType.ofLocal() == OsType.LINUX) {
            return;
        }

        AppOperationMode.switchToSyncOrThrow(AppOperationMode.GUI);

        for (AppLayoutModel.Entry entry : AppLayoutModel.get().getEntries()) {
            AppLayoutModel.get().getSelected().setValue(entry);
            ThreadHelper.sleep(1000);
        }
    }
}
