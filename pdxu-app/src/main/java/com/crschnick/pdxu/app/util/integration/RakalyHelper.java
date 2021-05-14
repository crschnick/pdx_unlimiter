package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.PdxuInstallation;

import java.io.IOException;
import java.nio.file.Path;

public class RakalyHelper {

    public static byte[] meltSavegame(Path file) throws Exception {
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
