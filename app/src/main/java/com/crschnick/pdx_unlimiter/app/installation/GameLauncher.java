package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdx_unlimiter.app.installation.*;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.IronyHelper;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.SavegameHelper;
import com.crschnick.pdx_unlimiter.app.util.SteamHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GameLauncher {

    private static void startLauncher() {
        var install = SavegameManagerState.get().current().getInstallation();
        if (Settings.getInstance().launchIrony.getValue()) {
            IronyHelper.launchEntry(ctx.getGame(), false);
        } else {
            install.getDistType().startLauncher();
        }
    }

    private static void startDirectly(SavegameEntry<?,?> e) {
        SavegameHelper.withSavegame(e, ctx -> {
            var install = ctx.getInstallation();
            if (Settings.getInstance().launchIrony.getValue()) {
                IronyHelper.launchEntry(ctx.getGame(), true);
            } else {
                boolean doLaunch = install.getDistType().checkDirectLaunch();
                if (doLaunch) {
                    ctx.getIntegration().getInstallation().startDirectly();
                }
            }
        });
    }

    public static <T, I extends SavegameInfo<T>> void launchSavegame(SavegameEntry<T,I> e) {
        SavegameHelper.withSavegame(e, ctx -> {
            var gi = ctx.getIntegration();
            if (!SavegameActions.isEntryCompatible(e)) {
                boolean startAnyway = GuiIncompatibleWarning.showIncompatibleWarning(
                        ctx.getIntegration().getInstallation(), e);
                if (!startAnyway) {
                    return;
                }
            }

            try {
                var path = ctx.getIntegration().getInstallation().getExportTarget(
                        ctx.getIntegration().getSavegameStorage(), e);
                ctx.getIntegration().getSavegameStorage().copySavegameTo(e, path);
                gi.getInstallation().writeLaunchConfig(e.getName(), ctx.getCollection().getLastPlayed(), path);
                ctx.getCollection().lastPlayedProperty().setValue(Instant.now());


                var dlcs = e.getInfo().getDlcs().stream()
                        .map(d -> gi.getInstallation().getDlcForName(d))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                if (ctx.getGame().equals(Game.STELLARIS)) {
                    writeStellarisDlcLoadFile(GameInstallation.STELLARIS, dlcs);
                } else {
                    var mods = e.getInfo().getMods().stream()
                            .map(m -> gi.getInstallation().getModForName(m))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    writeDlcLoadFile(ctx.getIntegration().getInstallation(), mods, dlcs);
                }


                startDirectly(e);
            } catch (Exception ex) {

                ErrorHandler.handleException(ex);
            }
        });
    }

    static List<GameMod> getEnabledMods(GameInstallation installation) throws IOException {
        var file = installation.getUserPath().resolve("dlc_load.json");
        var node = JsonHelper.read(file);
        return StreamSupport.stream(node.get("enabled_mods").spliterator(), false)
                .map(m -> installation.getModForName(m.textValue()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    static void writeStellarisDlcLoadFile(
            GameInstallation installation, List<GameDlc> dlcs) throws IOException {
        var existingMods = getEnabledMods(installation);
        writeDlcLoadFile(installation, existingMods, dlcs);
    }

    static void writeDlcLoadFile(GameInstallation installation, List<GameMod> mods, List<GameDlc> dlcs) throws IOException {
        var file = installation.getUserPath().resolve("dlc_load.json");
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        n.putArray("enabled_mods").addAll(mods.stream()
                .map(d -> FilenameUtils.separatorsToUnix
                        (installation.getUserPath().relativize(d.getModFile()).toString()))
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        n.putArray("disabled_dlcs").addAll(installation.getDlcs().stream()
                .filter(d -> d.isExpansion() && !dlcs.contains(d))
                .map(d -> FilenameUtils.separatorsToUnix(
                        installation.getPath().relativize(d.getInfoFilePath()).toString()))
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        JsonHelper.write(n, file);
    }
}
