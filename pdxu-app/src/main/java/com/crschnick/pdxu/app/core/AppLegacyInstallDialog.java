package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.WindowsRegistry;

import java.nio.file.Files;
import java.nio.file.Path;

public class AppLegacyInstallDialog {

    public static void showIfNeeded() {
        if (OsType.ofLocal() != OsType.WINDOWS) {
            return;
        }

        var exe = WindowsRegistry.of().readStringValueIfPresent(WindowsRegistry.HKEY_LOCAL_MACHINE, "SOFTWARE\\Classes\\pdxu\\DefaultIcon");
        if (exe.isEmpty()) {
            return;
        }

        var loc = Path.of(exe.get()).getParent();
        if (!Files.exists(loc)) {
            return;
        }

        var modal = ModalOverlay.of("legacyInstallTitle", AppDialog.dialogText(AppI18n.observable("legacyInstallContent")));
        modal.addButton(new ModalButton("keepLegacy", () -> {}, true, false));
        modal.addButton(new ModalButton("uninstallLegacy", () -> {
            LocalExec.readStdoutIfPossible("cmd", "/c", "start \"\" msiexec /x {9D873C37-DCDA-3F50-A52A-DAB1F1A43DAE}");
        }, true, true));
        modal.show();
    }
}
