package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.PdxuApp;
import com.crschnick.pdxu.app.core.ComponentManager;
import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Modality;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;


public class GuiEolDialog {

    public static void show() {
        var header = """
                The Pdx-Unlimiter v2 has reached the end of its life. It is succeeded by the Pdx-Unlimiter v3, which is a completely new application.
                
                It comes with:
                - Support for EU5
                - Fixes for CK3 1.18+
                - Fixes for VIC3 1.10+
                - A completely overhauled UI
                - Support for light/dark mode
                - Many more new appearance and applications behaviour settings
                - Signed Windows releases, meaning no more SmartScreen popup
                - An improved desktop icon
                - Faster startup times and improved memory usage
                - Support to run it in the system tray/background
                - Better scaling for hdpi displays
                - Fixes on Windows when pinning the executable to the taskbar
                - Properly working file associations, without confusing various browsers thinking that pdxu can open zip files
                
                Updates to v3 are not distributed through the old updater anymore. You can choose to update to v3 from here, which will launch the installer. Your save data will transfer seamlessly to v3.

                You can also keep using your current v2 version, you will however no longer receive any updates for it. All future Pdx-Unlimiter updates are only available in the v3 version.
                
                """;

        Alert alert = GuiDialogHelper.createEmptyAlert();
        alert.setWidth(600);
        alert.setHeight(730);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setResizable(true);
        alert.setTitle("Update notice");
        alert.getDialogPane().setHeaderText(header);
        alert.getDialogPane().getScene().getWindow()
                .setOnCloseRequest(e -> alert.setResult(ButtonType.CLOSE));


        var visitType = new ButtonType("View on GitHub", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().add(visitType);
        Button visitB = (Button) alert.getDialogPane().lookupButton(visitType);
        visitB.addEventFilter(
                ActionEvent.ACTION,
                e -> {
                    Hyperlinks.open(Hyperlinks.RELEASES);
                    e.consume();
                });

        var autoInstallSupported = SystemUtils.IS_OS_WINDOWS && !PdxuInstallation.getInstance().isStandalone();
        if (autoInstallSupported) {
            var ignoreType = new ButtonType("Ignore", ButtonBar.ButtonData.NO);
            alert.getButtonTypes().add(ignoreType);
            Button ignoreB = (Button) alert.getDialogPane().lookupButton(ignoreType);
            ignoreB.setOnAction(e -> {
                e.consume();
            });

            var installType = new ButtonType("Install", ButtonBar.ButtonData.APPLY);
            alert.getButtonTypes().add(installType);
            Button installB = (Button) alert.getDialogPane().lookupButton(installType);
            installB.setDefaultButton(true);
            installB.setOnAction(e -> {
                try {
                    var exec = Path.of(System.getenv("LOCALAPPDATA"), "Pdx-Unlimiter", "pdx-unlimiter.exe");
                    var url = "https://github.com/crschnick/pdx_unlimiter/releases/download/3.0.5/pdx-unlimiter-installer-windows-x86_64.msi";//"https://github.com/crschnick/pdx_unlimiter/releases/latest/download/pdx-unlimiter-installer-windows-x86_64.msi";
                    var pb = new ProcessBuilder("cmd", "/c", "set MSIFASTINSTALL=7&set DISABLEROLLBACK=1&start \"\" /wait msiexec /qb /i " + url + "&start \"\" \"" + exec + "\"");
                    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                    pb.start();
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                }
                System.exit(0);
                e.consume();
            });
        }

        alert.show();
    }
}
