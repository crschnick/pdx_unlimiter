package com.crschnick.pdx_unlimiter.app.util.integration;

import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RakalyHelper {

    public static Path meltSavegame(Path file) throws Exception {
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

        if (returnCode != 0) {
            throw new IOException("Rakaly melter failed with exit code " + returnCode);
        }

        Path temp = FileUtils.getTempDirectory().toPath()
                .resolve("pdxu").resolve("melted." + FilenameUtils.getExtension(file.toString()));
        FileUtils.forceMkdirParent(temp.toFile());
        Files.write(temp, b);
        return temp;
    }
}
