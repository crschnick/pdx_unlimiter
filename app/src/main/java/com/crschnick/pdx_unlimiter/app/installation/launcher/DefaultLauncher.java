package com.crschnick.pdx_unlimiter.app.installation.launcher;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameDlc;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.SavegameHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DefaultLauncher extends GameLauncher {

    @Override
    public String description() {
        return null;
    }

    @Override
    public void startParadoxLauncher(GameInstallation installation) {
        Path launcherPath = installation.getLauncherDataPath();
        Path bootstrapper = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            bootstrapper = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs")
                    .resolve("Paradox Interactive")
                    .resolve("bootstrapper-v2.exe");
        } else {
            bootstrapper = Path.of(System.getProperty("user.home"))
                    .resolve(".paradoxlauncher")
                    .resolve("bootstrapper-v2");
        }

        try {
            new ProcessBuilder()
                    .directory(launcherPath.toFile())
                    .command(bootstrapper.toString(),
                            "--pdxlGameDir", launcherPath.toString(),
                            "--gameDir", launcherPath.toString())
                    .start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public void startDirectly(SavegameEntry<?, ?> entry) {
        SavegameHelper.withSavegame(entry, ctx -> {
            if (!SavegameActions.isEntryCompatible(entry)) {
                boolean startAnyway = GuiIncompatibleWarning.showIncompatibleWarning(
                        ctx.getIntegration().getInstallation(), entry);
                if (!startAnyway) {
                    return;
                }
            }

            boolean stellaris = ctx.getIntegration().getInstallation()
                    .equals(GameInstallation.ALL.get(Game.STELLARIS));
            if (stellaris) {
                GuiIncompatibleWarning.showStellarisModWarning();
            }

            LauncherHelper.launchCampaignEntry(entry);

            try {
                ctx.getIntegration().getInstallation().startDirectly();
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
    }
}
