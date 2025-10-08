package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.core.AppInstallation;
import com.crschnick.pdxu.app.savegame.SavegameContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RakalyHelper {


    public static boolean shouldShowButton(SavegameContext<?, ?> context) {
        return true;
    }

    public static byte[] toMeltedPlaintext(Path file) throws Exception {
        var melter = AppInstallation.ofCurrent().getRakalyExecutable();
        check(melter);
        var proc = new ProcessBuilder(
                melter.toString(),
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

    public static byte[] toEquivalentPlaintext(Path file) throws Exception {
        var melter = AppInstallation.ofCurrent().getRakalyExecutable();
        check(melter);
        var proc = new ProcessBuilder(
                melter.toString(),
                "melt",
                "--unknown-key", "stringify",
                "--retain",
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

    private static void check(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("Ironman melter executable " + file + " is missing. This is usually caused by Windows Defender or another AntiVirus program removing the melter executable because it thinks it's malicious.\n\nYou can try deleting %LOCALAPPDATA%\\Programs\\Pdx-Unlimiter\\app\\ and relaunching pdxu. That should trigger a new download. If the file then gets removed again, you probably have to add an exception to your antivirus.");
        }
    }
}
