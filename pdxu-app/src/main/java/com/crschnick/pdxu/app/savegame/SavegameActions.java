package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.window.AppSideWindow;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.DesktopHelper;
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.app.util.RakalyHelper;
import com.crschnick.pdxu.io.savegame.SavegameParseResult;
import com.crschnick.pdxu.io.savegame.SavegameType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class SavegameActions {

    public static <T, I extends SavegameInfo<T>> Optional<Path> exportToTemp(SavegameEntry<T, I> entry, boolean includeEntryName) {
        return Optional.ofNullable(SavegameContext.mapSavegame(entry, ctx -> {
            var target = FileExportTarget.createExportTarget(
                    FileUtils.getTempDirectory().toPath(), includeEntryName, entry);

            Path out;
            try {
                out = target.export();
            } catch (Exception ioException) {
                ErrorEventFactory.fromThrowable(ioException).handle();
                return null;
            }
            return out;
        }));
    }

    public static <T, I extends SavegameInfo<T>> void openSavegame(SavegameEntry<T, I> entry) {
        SavegameContext.withSavegameContext(entry, ctx -> {
            DesktopHelper.browsePath(ctx.getStorage().getSavegameDataDirectory(entry));
        });
    }

    public static <T, I extends SavegameInfo<T>> void exportSavegame(SavegameEntry<T, I> e) {
        SavegameContext.withSavegameContext(e, ctx -> {
            if (ctx.getInfo() == null) {
                return;
            }

            try {
                FileExportTarget.createExportTarget(e).export();
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).handle();
            }
        });
    }

    public static <T, I extends SavegameInfo<T>> void branch(SavegameEntry<T, I> entry) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegameInfoContextAsync(entry, ctx -> {
                ctx.getStorage().createNewBranch(entry);
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> Image createImageForEntry(SavegameEntry<T, I> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            return ctx.getGuiFactory().tagImage(entry.getInfo(), entry.getInfo().getData().getTag());
        });
    }

    public static void loadLatestSavegameCheckpoint(Game g) {
        var savegames = SavegameWatcher.ALL.get(g).getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        SavegameType type = SavegameStorage.get(g).getType();
        SavegameParseResult r = null;
        try {
            var file = savegames.getFirst().path;
            var bytes = Files.readAllBytes(file);
            if (type.isBinary(bytes)) {
                bytes = RakalyHelper.toEquivalentPlaintext(file);
            }
            var struc = type.determineStructure(bytes);
            r = struc.parse(bytes);
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
        }

        if (r == null) {
            return;
        }

        r.visit(new SavegameParseResult.Visitor() {
            @Override
            public void success(SavegameParseResult.Success s) {
                try {
                    var targetUuuid = savegames.getFirst().getCampaignIdOverride()
                            .orElse(type.getCampaignIdHeuristic(s.content));
                    SavegameStorage.get(g).getSavegameCampaign(targetUuuid)
                            .flatMap(col -> col.entryStream().findFirst()).ifPresent(entry -> {
                        TaskExecutor.getInstance().submitTask(() -> {
                            SavegameStorage.get(g).loadEntry(entry);
                            SavegameContext.withSavegameContext(entry, ctx -> {
                                GameDistLauncher.continueSavegame(entry, false);
                            });
                        }, true);
                    });
                } catch (Exception e) {
                    ErrorEventFactory.fromThrowable(e).handle();
                }
            }
        });
    }

    public static void importLatestSavegame(Game g) {
        var savegames = SavegameWatcher.ALL.get(g).getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        FileImporter.importTargets(Set.of(savegames.getFirst()));
    }

    public static void importLatestAndLaunch(Game g) {
        var savegames = SavegameWatcher.ALL.get(g).getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        var target = savegames.getFirst();
        var checksum = target.getSourceFileChecksum();
        savegames.getFirst().importTarget(s -> {
            if (s.isEmpty()) {
                SavegameStorage.get(g).getEntryForSourceFileChecksum(checksum).ifPresent(e -> {
                    // The info is loaded asynchronously only when the savegame is opened in the gui.
                    // This means that at this point, the info can either be null or not null
                    // In case it is null, temporarily set it
                    if (e.infoProperty().get() == null) {
                        SavegameStorage.get(g).loadEntry(e);
                        GameDistLauncher.continueSavegame(e, false);
                        e.unload();
                    } else {
                        GameDistLauncher.continueSavegame(e, false);
                    }
                });
            }
        });
    }

    public static <T, I extends SavegameInfo<T>> void meltSavegame(SavegameEntry<T, I> e) {
        Alert alert = AppSideWindow.createEmptyAlert();
        alert.setAlertType(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Melt savegame");
        alert.setHeaderText("""
                Do you want to convert the selected savegame into a non-ironman savegame using the Rakaly melter?
                """);
        alert.setContentText("""
                The original savegame will not get modified.""");
        Optional<ButtonType> result = alert.showAndWait();
        if (!(result.isPresent() && result.get().getButtonData().isDefaultButton())) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
                ctx.getStorage().melt(ctx.getEntry());
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void delete(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegameContext(e, ctx -> {
                ctx.getStorage().delete(e);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void delete(SavegameCampaign<T, I> c) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withCollectionContext(c, ctx -> {
                ctx.getStorage().delete(c);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void editSavegame(SavegameEntry<T, I> e) {
        SavegameContext.withSavegameContext(e, ctx -> {
            EditorProvider.get().openSavegame(ctx.getStorage(), e);
        });
    }

    public static <T, I extends SavegameInfo<T>> void reloadSavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegameContext(e, ctx -> {
                TrackEvent.debug("Reloading savegame");
                e.unload();
                ctx.getStorage().invalidateSavegameInfo(e);
                ctx.getStorage().loadEntry(e);
            });
        }, false);
    }

    public static <T, I extends SavegameInfo<T>> void copySavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegameContext(e, ctx -> {
                var in = ctx.getStorage().getSavegameFile(e);
                var campaignId = ctx.getCollection().getUuid();
                ctx.getStorage().importSavegame(in, false, null, campaignId);
            });
        }, true);
    }
}
