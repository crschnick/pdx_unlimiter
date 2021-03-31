package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameLauncher;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.app.util.integration.RakalyHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class SavegameActions {

    public static <T, I extends SavegameInfo<T>> Optional<Path> exportToTemp(SavegameEntry<T, I> entry) {
        return Optional.ofNullable(SavegameContext.mapSavegame(entry, ctx -> {
            var sc = ctx.getStorage();
            var out = FileUtils.getTempDirectory().toPath().resolve(
                    sc.getFileSystemCompatibleName(entry, true));
            try {
                sc.copySavegameTo(entry, out);
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                return null;
            }
            return out;
        }));
    }

    public static boolean isEntryCompatible(SavegameEntry<?, ?> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            var info = ctx.getInfo();
            if (info == null) {
                return false;
            }

            var ins = ctx.getInstallation();
            boolean missingMods = info.getMods().stream()
                    .map(ins::getModForName)
                    .anyMatch(Optional::isEmpty);

            boolean missingDlc = info.getDlcs().stream()
                    .map(ins::getDlcForName)
                    .anyMatch(Optional::isEmpty);

            return areCompatible(ins.getVersion(), info.getVersion()) &&
                    !missingMods && !missingDlc;
        });
    }

    public static boolean isVersionCompatible(SavegameInfo<?> info) {
        return areCompatible(
                GameInstallation.ALL.get(SavegameManagerState.get().current()).getVersion(),
                info.getVersion());
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public static <T, I extends SavegameInfo<T>> void openSavegame(SavegameEntry<T, I> entry) {
        SavegameContext.withSavegame(entry, ctx -> {
            ThreadHelper.open(ctx.getStorage().getSavegameDataDirectory(entry));
        });
    }

    public static <T, I extends SavegameInfo<T>> void exportSavegame(SavegameEntry<T, I> e) {
        SavegameContext.withSavegame(e, ctx -> {
            try {
                var path = ctx.getInstallation().getExportTarget(e);
                ctx.getStorage().copySavegameTo(e, path);
            } catch (IOException ex) {
                ErrorHandler.handleException(ex);
            }
        });
    }

    public static <T, I extends SavegameInfo<T>> void moveEntry(
            SavegameCollection<T, I> collection, SavegameEntry<T, I> entry) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(entry, ctx -> {
                ctx.getStorage().moveEntry(collection, entry);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> Image createImageForEntry(SavegameEntry<T, I> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            return ctx.getGuiFactory().tagImage(entry.getInfo(), entry.getInfo().getTag());
        });
    }

    public static void importLatestSavegame() {
        var savegames = SavegameWatcher.ALL.get(
                SavegameManagerState.get().current()).getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        FileImporter.importTargets(Set.of(savegames.get(0)));
    }

    public static void importLatestAndLaunch() {
        var savegames = SavegameWatcher.ALL.get(
                SavegameManagerState.get().current()).getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        savegames.get(0).importTarget(s -> {
            s.visit(new SavegameParser.StatusVisitor<>() {
                @Override
                @SuppressWarnings("unchecked")
                public void success(SavegameParser.Success<SavegameInfo<?>> s) {
                    SavegameStorage.get(SavegameManagerState.get().current())
                            .getSavegameForChecksum(s.checksum)
                            .ifPresent(e -> {
                                // The info is loaded asynchronously when the savegame is opened in the gui.
                                // This means that at this point, the info can either be null or not null
                                // In case it is null, temporarily set it
                                if (e.infoProperty().get() == null) {
                                    e.load((SavegameInfo<Object>) s.info);
                                    GameLauncher.continueSavegame(e);
                                    e.unload();
                                }
                            });
                }
            });
        });
    }

    public static <T, I extends SavegameInfo<T>> void meltSavegame(SavegameEntry<T, I> e) {
        if (!GuiDialogHelper.showMeltDialog()) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                Path meltedFile;
                try {
                    meltedFile = RakalyHelper.meltSavegame(ctx.getStorage().getSavegameFile(e));
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                    return;
                }
                var folder = ctx.getStorage().getOrCreateFolder("Melted savegames");
                folder.ifPresent(f -> {
                    ctx.getStorage().importSavegame(meltedFile, null, true, null, f);
                });
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void delete(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                ctx.getStorage().delete(e);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void delete(SavegameCollection<T, I> c) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withCollection(c, ctx -> {
                ctx.getStorage().delete(c);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void editSavegame(SavegameEntry<T, I> e) {
        EditorProvider.openEntry(e);
    }

    public static <T, I extends SavegameInfo<T>> void reloadSavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                ctx.getStorage().reloadSavegameAsync(e);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void copySavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                var sgs = ctx.getStorage();
                var in = sgs.getSavegameFile(e);
                sgs.importSavegame(in, "Copy of " + e.getName(), false, null,
                        sgs.getSavegameCollection(e));
            });
        }, false);
    }
}
