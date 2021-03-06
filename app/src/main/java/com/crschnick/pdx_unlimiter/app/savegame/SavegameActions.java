package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.editor.EditTarget;
import com.crschnick.pdx_unlimiter.app.editor.Editor;
import com.crschnick.pdx_unlimiter.app.editor.StorageEditTarget;
import com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.util.RakalyHelper;
import com.crschnick.pdx_unlimiter.app.util.SavegameHelper;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SavegameActions {

    public static <T, I extends SavegameInfo<T>> Optional<Path> exportToTemp(SavegameEntry<T, I> entry) {
        return Optional.ofNullable(SavegameHelper.mapSavegame(entry, ctx -> {
            var sc = ctx.getIntegration().getSavegameStorage();
            var out = FileUtils.getTempDirectory().toPath().resolve(sc.getFileName(entry));
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
        return SavegameHelper.mapSavegame(entry, ctx -> {
            var gi = ctx.getIntegration();
            var info = ctx.getInfo();
            if (info == null) {
                return false;
            }

            var ins = gi.getInstallation();
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
                SavegameManagerState.get().current().getInstallation().getVersion(),
                info.getVersion());
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public static <T, I extends SavegameInfo<T>> void openCampaignEntry(SavegameEntry<T, I> entry) {
        ThreadHelper.open(SavegameManagerState.<T, I>get().current().getSavegameStorage().getPath(entry));
    }

    public static <T, I extends SavegameInfo<T>> Optional<Path> exportCampaignEntry(SavegameEntry<T,I> e) {
        return SavegameHelper.mapSavegame(e, ctx -> {
            try {
                var path = ctx.getIntegration().getInstallation().getExportTarget(
                        ctx.getIntegration().getSavegameStorage(), e);
                ctx.getIntegration().getSavegameStorage().copySavegameTo(e, path);
                return Optional.of(path);
            } catch (IOException ex) {
                ErrorHandler.handleException(ex);
                return Optional.empty();
            }
        });
    }

    public static <T, I extends SavegameInfo<T>> void moveEntry(
            SavegameCollection<T, I> collection, SavegameEntry<T, I> entry) {
        var s = SavegameManagerState.<T, I>get();
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withSavegame(entry, ctx -> {
                ctx.getIntegration().getSavegameStorage().moveEntry(collection, entry);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> Image createImageForEntry(SavegameEntry<T, I> entry) {
        @SuppressWarnings("unchecked")
        Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameStorage().contains(entry))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        var g = gi.orElseThrow(IllegalArgumentException::new);
        return g.getGuiFactory().tagImage(entry.getInfo(), entry.getInfo().getTag());
    }

    public static <T, I extends SavegameInfo<T>> void launchCampaignEntry(SavegameEntry<T,I> e) {
        SavegameHelper.withSavegame(e, ctx -> {
            var gi = ctx.getIntegration();
            if (!isEntryCompatible(e)) {
                boolean startAnyway = gi.getGuiFactory().displayIncompatibleWarning(e);
                if (!startAnyway) {
                    return;
                }
            }

            Optional<Path> p = exportCampaignEntry(e);
            if (p.isPresent()) {
                try {
                    gi.getInstallation().writeLaunchConfig(e.getName(), ctx.getCollection().getLastPlayed(), p.get());

                    var mods = e.getInfo().getMods().stream()
                            .map(m -> gi.getInstallation().getModForName(m))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    var dlcs = e.getInfo().getDlcs().stream()
                            .map(d -> gi.getInstallation().getDlcForName(d))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    gi.getInstallation().writeDlcLoadFile(mods, dlcs);

                    ctx.getCollection().lastPlayedProperty().setValue(Instant.now());
                    ctx.getIntegration().getInstallation().startDirectly();
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                }
            }
        });
    }

    public static void importLatestSavegame() {
        var savegames = SavegameManagerState.get().current().getSavegameWatcher().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        FileImporter.importTargets(Set.of(savegames.get(0)));
    }

    public static void importLatestAndLaunch() {
        var gi = SavegameManagerState.get().current();
        var savegames = SavegameManagerState.get().current().getSavegameWatcher().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        savegames.get(0).importTarget(s -> {
            s.visit(new SavegameParser.StatusVisitor<>() {
                @Override
                public void success(SavegameParser.Success<SavegameInfo<?>> s) {
                    gi.getSavegameStorage().getSavegameForChecksum(s.checksum)
                            .ifPresent(e -> {
                                SavegameManagerState.get().selectEntry(e);
                                launchCampaignEntry(e);
                    });
                }
            });
        });
    }

    public static <T, I extends SavegameInfo<T>> void meltSavegame(SavegameEntry<T, I> e) {
        if (!DialogHelper.showMeltDialog()) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withSavegame(e, ctx -> {
                var gi = ctx.getIntegration();
                Path meltedFile;
                try {
                    meltedFile = RakalyHelper.meltSavegame(gi.getSavegameStorage().getSavegameFile(e));
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                    return;
                }
                var folder = gi.getSavegameStorage()
                        .getOrCreateFolder("Melted savegames");
                folder.ifPresent(f -> {
                    gi.getSavegameStorage().importSavegame(meltedFile, null, true, f);
                });
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void delete(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withSavegame(e, ctx -> {
                ctx.getIntegration().getSavegameStorage().delete(e);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void delete(SavegameCollection<T, I> c) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withCollection(c, gi -> {
                gi.getSavegameStorage().delete(c);
                if (SavegameManagerState.get().globalSelectedCampaignProperty().get() == c) {
                    SavegameManagerState.get().selectCollection(null);
                }
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void editSavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withSavegame(e, ctx -> {
                var in = ctx.getIntegration().getSavegameStorage().getSavegameFile(e);
                var target = EditTarget.create(in);
                target.ifPresent(t -> {
                    var storageTarget = new StorageEditTarget<>(ctx.getIntegration().getSavegameStorage(), e, t);
                    Editor.createNewEditor(storageTarget);
                });
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void reloadSavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withSavegame(e, ctx -> {
                var sgs = ctx.getIntegration().getSavegameStorage();
                sgs.reloadSavegameAsync(e);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void copySavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameHelper.withSavegame(e, ctx -> {
                var sgs = ctx.getIntegration().getSavegameStorage();
                var in = sgs.getSavegameFile(e);
                sgs.importSavegame(in, "Copy of " + e.getName(), false, sgs.getSavegameCollection(e));
            });
        }, false);
    }
}
