package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.RakalyHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.io.node.NodeEvaluator;
import com.crschnick.pdxu.io.parser.TextFormatParser;

public class AppAotTrain {

    public static void runTrainingMode() throws Throwable {
        // Some runners don't support graphics properly
        var graphics = OsType.ofLocal() != OsType.LINUX
                && !(OsType.ofLocal() == OsType.MACOS
                        && AppProperties.get().getArch().equals("x86_64"));
        if (graphics) {
            AppOperationMode.switchToSyncOrThrow(AppOperationMode.GUI);
            ThreadHelper.sleep(5000);

            // Pages are only built once they are shown, so visit all of them
            for (AppLayoutModel.Entry entry : AppLayoutModel.get().getEntries()) {
                AppLayoutModel.get().getSelected().setValue(entry);
                ThreadHelper.sleep(1000);
            }
        }

        if (OsType.ofLocal() == OsType.WINDOWS) {
            var rootDir = AppInstallation.ofCurrent()
                    .getBaseInstallationPath()
                    .getParent()
                    .getParent()
                    .getParent()
                    .getParent();
            var save = rootDir.resolve("misc").resolve("train_save.eu5");
            var bytes = RakalyHelper.toEquivalentPlaintext(save);
            var parsed = TextFormatParser.eu5().parse("train_save", bytes, 0, true);
            NodeEvaluator.evaluateArrayNode(parsed);
        }
    }
}
