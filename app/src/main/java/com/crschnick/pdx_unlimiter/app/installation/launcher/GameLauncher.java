package com.crschnick.pdx_unlimiter.app.installation.launcher;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.SavegameHelper;

import java.io.IOException;

public abstract class GameLauncher {

    public static void startDirectly(SavegameEntry<?,?> entry) {

    }

    public abstract String description();

    public abstract void startParadoxLauncher(GameInstallation installation);

    protected final void verifyCompatibility(SavegameEntry<?,?> entry) {
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

    public abstract void startDirectly(SavegameEntry<?,?> entry);
}
