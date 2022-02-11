package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.model.ck3.Ck3SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RakalyHelper {

    /**
     * Temp for ck3 melter fix.
     */
    public static Path meltSavegame(SavegameContext<?,?> infoContext) throws Exception {
        var melter = PdxuInstallation.getInstance().getRakalyExecutable();
        if (infoContext.getGame() == Game.CK3) {
            Ck3SavegameInfo ck3Info = (Ck3SavegameInfo) infoContext.getInfo();
            if (ck3Info.getVersion().getSecond() < 5) {
                melter = PdxuInstallation.getInstance().getRakalyCk3LegacyExecutable();
                LoggerFactory.getLogger(RakalyHelper.class).info("Using legacy CK3 melter");
            }
        }

        var file = infoContext.getStorage().getSavegameFile(infoContext.getEntry()).toString();
        var proc = new ProcessBuilder(
                melter.toString(),
                "melt",
                "--unknown-key", "stringify",
                "--to-stdout",
                file)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        var b = proc.getInputStream().readAllBytes();
        proc.waitFor();
        int returnCode = proc.exitValue();

        if (returnCode != 0 && returnCode != 1) {
            throw new IOException("Rakaly melter failed with exit code " + returnCode);
        }

        Path temp = FileUtils.getTempDirectory().toPath()
                .resolve("pdxu").resolve("melted." + FilenameUtils.getExtension(file.toString()));
        FileUtils.forceMkdirParent(temp.toFile());
        Files.write(temp, b);
        return temp;
    }

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

        if (returnCode != 0 && returnCode != 1) {
            throw new IOException("Rakaly melter failed with exit code " + returnCode);
        }

        Path temp = FileUtils.getTempDirectory().toPath()
                .resolve("pdxu").resolve("melted." + FilenameUtils.getExtension(file.toString()));
        FileUtils.forceMkdirParent(temp.toFile());
        Files.write(temp, b);
        return temp;
    }

    public static byte[] toPlaintext(Path file) throws Exception {
        var proc = new ProcessBuilder(
                PdxuInstallation.getInstance().getRakalyExecutable().toString(),
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
}
