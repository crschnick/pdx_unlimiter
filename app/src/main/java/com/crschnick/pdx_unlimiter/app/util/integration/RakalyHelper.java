package com.crschnick.pdx_unlimiter.app.util.integration;

import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiDialogHelper;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RakalyHelper {

    private static void showError() {
        GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.setTitle("Rakaly error");
            alert.setHeaderText("It seems like a core component, the Rakaly Ironman converter, is missing.\n" +
                    "This is usually caused by a not fully completed update.\n" +
                    "Please try restarting the Pdx-Unlimiter while you are connected to the internet to fix this.\n\n" +
                    "If this doesn't help, please report this error to the developers.");
        });
    }

    public static byte[] meltSavegame(Path file) throws Exception {
        if (!Files.exists(PdxuInstallation.getInstance().getRakalyExecutable())) {
            showError();
            throw new IOException("Rakaly melter missing");
        }

        var proc = new ProcessBuilder(
                PdxuInstallation.getInstance().getRakalyExecutable().toString(),
                "melt",
                "--unknown-key", "stringify",
                "--to-stdout",
                file.toString())
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        var b = proc.getInputStream().readAllBytes();
        proc.waitFor();
        int returnCode = proc.exitValue();

        if (returnCode != 0 && returnCode != 1) {
            throw new IOException("Rakaly melter failed with exit code " + returnCode);
        }

        return b;
    }
}
