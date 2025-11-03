package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.app.util.WindowsRegistry;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AppLegacyInstallDialog {

    public static void showIfNeeded() {
        try {
            var updateFile = AppProperties.get().getDataDir().resolve("update");
            if (Files.exists(updateFile)) {
                Files.writeString(updateFile, "false");
            }

            var errorExitFile = AppProperties.get().getDataDir().resolve("error_exit");
            if (Files.exists(errorExitFile)) {
                Files.writeString(errorExitFile, "false");
            }
        } catch (IOException ex) {
            ErrorEventFactory.fromThrowable(ex).omit().handle();
        }

        if (OsType.ofLocal() != OsType.WINDOWS) {
            return;
        }

        var exe = WindowsRegistry.of().readStringValueIfPresent(WindowsRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\pdxu\\DefaultIcon");
        if (exe.isEmpty()) {
            deleteLegacyLauncherDataIfNeeded();
            return;
        }

        var loc = Path.of(exe.get()).getParent();
        if (!Files.exists(loc)) {
            deleteLegacyLauncherDataIfNeeded();
            return;
        }

        var modal = ModalOverlay.of("legacyInstallTitle", AppDialog.dialogText(AppI18n.observable("legacyInstallContent")));
        modal.addButton(new ModalButton("keepLegacy", () -> {}, true, false));
        modal.addButton(new ModalButton("uninstallLegacy", () -> {
            ThreadHelper.runAsync(() -> {
                LocalExec.readStdoutIfPossible("cmd", "/c", "start \"\" /wait msiexec /x {9D873C37-DCDA-3F50-A52A-DAB1F1A43DAE}");
                deleteLegacyLauncherDataIfNeeded();
            });
        }, true, true));
        modal.show();
    }

    private static void deleteLegacyLauncherDataIfNeeded() {
        var path = AppSystemInfo.ofWindows().getLocalAppData().resolve("Programs").resolve("Pdx-Unlimiter");
        if (Files.exists(path)) {
            FileUtils.deleteQuietly(path.toFile());
        }
    }
}
