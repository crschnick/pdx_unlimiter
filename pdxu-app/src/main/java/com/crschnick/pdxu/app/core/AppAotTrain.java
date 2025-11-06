package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.RakalyHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;

public class AppAotTrain {

    public static void runTrainingMode() throws Throwable {
        // Linux runners don't support graphics
        if (OsType.ofLocal() != OsType.LINUX) {
            AppOperationMode.switchToSyncOrThrow(AppOperationMode.GUI);
            for (AppLayoutModel.Entry entry : AppLayoutModel.get().getEntries()) {
                AppLayoutModel.get().getSelected().setValue(entry);
                ThreadHelper.sleep(1000);
            }
        }

        // TODO: This crashes the JVM on some systems. Why?
//        if (OsType.ofLocal() == OsType.WINDOWS) {
//            var rootDir = AppInstallation.ofCurrent()
//                    .getBaseInstallationPath()
//                    .getParent()
//                    .getParent()
//                    .getParent()
//                    .getParent();
//            var save = rootDir.resolve("misc").resolve("train_save.eu5");
//            var bytes = RakalyHelper.toEquivalentPlaintext(save);
//            var parsed = TextFormatParser.eu5().parse("train_save", bytes, 0, true);
//            NodeEvaluator.evaluateArrayNode(parsed);
//        }
    }
}
