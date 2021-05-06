package com.crschnick.pdx_unlimiter.app.installation.dist;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiSavegameNotes;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameDlc;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.savegame.*;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.integration.IronyHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GameDistLauncher {

    public static void startLauncher() {
        try {
            var game = SavegameManagerState.get().current();
            if (Settings.getInstance().launchIrony.getValue()) {
                IronyHelper.launchEntry(game, false);
            } else {
                GameInstallation.ALL.get(game).getDist().startLauncher();
            }
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }
    }

    public static void startLauncherWithContinueGame(SavegameEntry<?, ?> e) {
        GuiSavegameNotes.showSavegameNotesReminderDialog(e.getNotes());

        try {
            setupContinueGame(e);
            startLauncherDirectly();
        } catch (Exception ex) {
            ErrorHandler.handleException(ex);
        }
    }

    public static <T, I extends SavegameInfo<T>> void continueSavegame(SavegameEntry<T, I> e, boolean debug) {
        SavegameContext.withSavegame(e, ctx -> {
            if (SavegameCompatibility.determineForEntry(e) != SavegameCompatibility.Compatbility.COMPATIBLE) {
                boolean startAnyway = GuiIncompatibleWarning.showIncompatibleWarning(
                        ctx.getInstallation(), e);
                if (!startAnyway) {
                    return;
                }
            }

            GuiSavegameNotes.showSavegameNotesReminderDialog(e.getNotes());

            try {
                setupContinueGame(e);
                startGameDirectly(e, debug);
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        });
    }

    private static <T, I extends SavegameInfo<T>> void setupContinueGame(SavegameEntry<T, I> e) throws Exception {
        var ctx = SavegameContext.getContext(e);
        var exportTarget = FileExportTarget.createExportTarget(e);
        var path = exportTarget.export();
        ctx.getInstallation().getType().writeLaunchConfig(
                ctx.getInstallation().getUserDir(),
                ctx.getStorage().getEntryName(e),
                ctx.getCollection().getLastPlayed(),
                path);
        ctx.getCollection().lastPlayedProperty().setValue(Instant.now());

        var dlcs = e.getInfo().getDlcs().stream()
                .map(d -> ctx.getInstallation().getDlcForName(d))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        var mods = e.getInfo().getMods().stream()
                .map(m -> ctx.getInstallation().getModForId(m))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (ctx.getGame().equals(Game.STELLARIS)) {
            writeStellarisDlcLoadFile(GameInstallation.ALL.get(Game.STELLARIS), dlcs);
        } else {
            writeDlcLoadFile(ctx.getInstallation(), mods, dlcs);
        }
    }

    private static void startLauncherDirectly() throws IOException {
        var game = SavegameManagerState.get().current();
        if (Settings.getInstance().launchIrony.getValue()) {
            IronyHelper.launchEntry(game, true);
        } else {
            GameInstallation.ALL.get(game).getDist().startLauncher();
        }
    }

    private static void startGameDirectly(SavegameEntry<?, ?> e, boolean debug) throws Exception {
        var ctx = SavegameContext.getContext(e);
        if (ctx.getGame().equals(Game.STELLARIS)) {
            var r = GuiIncompatibleWarning.showStellarisModWarning(
                    getEnabledMods(GameInstallation.ALL.get(Game.STELLARIS)));
            if (r.isPresent()) {
                var b = r.get();
                if (b) {
                    ctx.getInstallation().startDirectly(debug);
                } else {
                    startLauncherDirectly();
                }
            }
            return;
        }

        if (Settings.getInstance().launchIrony.getValue()) {
            IronyHelper.launchEntry(ctx.getGame(), true);
        } else {
            var install = ctx.getInstallation();
            ctx.getInstallation().startDirectly(debug);
        }
    }

    private static void writeStellarisDlcLoadFile(
            GameInstallation installation, List<GameDlc> dlcs) throws Exception {
        var existingMods = getEnabledMods(installation);
        writeDlcLoadFile(installation, existingMods, dlcs);
    }

    private static void writeDlcLoadFile(GameInstallation installation, List<GameMod> mods, List<GameDlc> dlcs) throws IOException {
        var file = installation.getUserDir().resolve("dlc_load.json");
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        n.putArray("enabled_mods").addAll(mods.stream()
                .map(d -> FilenameUtils.separatorsToUnix
                        (installation.getUserDir().relativize(d.getModFile()).toString()))
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        n.putArray("disabled_dlcs").addAll(installation.getDlcs().stream()
                .filter(d -> d.isExpansion() && !dlcs.contains(d))
                .map(d -> FilenameUtils.separatorsToUnix(
                        installation.getInstallDir().relativize(d.getInfoFilePath()).toString()))
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        JsonHelper.write(n, file);
    }

    private static List<GameMod> getEnabledMods(GameInstallation installation) throws Exception {
        var file = installation.getUserDir().resolve("dlc_load.json");
        var node = JsonHelper.read(file);
        return StreamSupport.stream(node.required("enabled_mods").spliterator(), false)
                .map(n -> installation.getModForId(n.textValue()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
