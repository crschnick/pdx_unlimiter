package com.crschnick.pdxu.app.installation.dist;

import com.crschnick.pdxu.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdxu.app.gui.dialog.GuiSavegameNotes;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.*;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.savegame.*;
import com.crschnick.pdxu.app.util.IronyHelper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameDistLauncher {

    public static void startLauncher(Game game) {
        try {
            if (AppPrefs.get().launchIrony().getValue()) {
                IronyHelper.launchEntry(game, false);
            } else {
                GameInstallation.ALL.get(game).getDist().startLauncher(Map.of());
            }
        } catch (IOException ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
        }
    }

    public static void startLauncherWithContinueGame(SavegameEntry<?, ?> e) {
        GuiSavegameNotes.showSavegameNotesReminderDialog(e.getNotes());

        try {
            setupContinueGame(e);
            startLauncherDirectly(e);
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
        }
    }

    public static <T, I extends SavegameInfo<T>> void continueSavegame(SavegameEntry<T, I> e, boolean debug) {
        SavegameContext.withSavegameContext(e, ctx -> {
            if (ctx.getInfo() == null) {
                return;
            }

            if (SavegameCompatibility.determineForModsAndDLCs(e) != SavegameCompatibility.Compatbility.COMPATIBLE ||
                    SavegameCompatibility.determineForVersion(ctx.getGame(), e.getInfo().getData().getVersion()) != SavegameCompatibility.Compatbility.COMPATIBLE) {
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
                ErrorEventFactory.fromThrowable(ex).handle();
            }
        });
    }

    private static <T, I extends SavegameInfo<T>> void setupContinueGame(SavegameEntry<T, I> e) throws Exception {
        var ctxOpt = SavegameContext.getContextIfExistent(e).filter(c -> c.getInfo() != null);
        if (ctxOpt.isEmpty()) {
            return;
        }

        var ctx = ctxOpt.get();
        var exportTarget = FileExportTarget.createExportTarget(e);
        var path = exportTarget.export();
        ctx.getInstallation().getType().writeLaunchConfig(
                ctx.getInstallation().getUserDir(),
                ctx.getStorage().getEntryName(e),
                ctx.getCollection().getLastPlayed(),
                path, ctx.getInfo().getData().getVersion()
        );
        ctx.getCollection().lastPlayedProperty().setValue(Instant.now());

        var dlcs = ctx.getInfo().getData().getDlcs() != null ? ctx.getInfo().getData().getDlcs().stream()
                .map(d -> ctx.getInstallation().getDlcForSavegameId(d))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()) : List.<GameDlc>of();
        var mods = ctx.getInfo().getData().getMods() != null ? ctx.getInfo().getData().getMods().stream()
                .map(m -> ctx.getInstallation().getModForSavegameId(m))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList()) : List.<GameMod>of();

        ctx.getInstallation().getType().writeModAndDlcLoadFile(ctx.getInstallation(), mods, dlcs);
    }

    public static boolean canChangeMods(Game game) {
        return AppPrefs.get().launchIrony().getValue() ||
                GameInstallation.ALL.get(game).getDist().supportsLauncher();
    }

    private static void startLauncherDirectly(SavegameEntry<?, ?> e) throws IOException {
        var game = SavegameContext.getForSavegame(e);
        if (AppPrefs.get().launchIrony().getValue()) {
            IronyHelper.launchEntry(game, true);
        } else {
            if (!GameInstallation.ALL.get(game).getDist().supportsLauncher()) {
                return;
            }

            GameInstallation.ALL.get(game).getDist().startLauncher(Map.of());
        }
    }

    private static void startGameDirectly(SavegameEntry<?, ?> e, boolean debug) throws Exception {
        var ctx = SavegameContext.getContext(e);
        if (ctx.getGame().getInstallType().getModInfoStorageType() ==
                GameInstallType.ModInfoStorageType.SAVEGAME_DOESNT_STORE_INFO) {
            var r = GuiIncompatibleWarning.showNoSavedModsWarning(
                    ctx.getGame(), ctx.getInstallation().queryEnabledMods());
            if (r.isPresent()) {
                var b = r.get();
                if (b) {
                    ctx.getInstallation().startDirectly(debug);
                } else {
                    startLauncherDirectly(e);
                }
            }
            return;
        }

        if (AppPrefs.get().launchIrony().getValue()) {
            IronyHelper.launchEntry(ctx.getGame(), true);
        } else {
            ctx.getInstallation().startDirectly(debug);
        }
    }
}
