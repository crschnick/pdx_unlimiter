package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.editor.Editor;
import com.crschnick.pdxu.app.editor.target.EditTarget;
import com.crschnick.pdxu.app.editor.target.StorageEditTarget;
import com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.model.SavegameInfo;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class SavegameActions {

    public static <T, I extends SavegameInfo<T>> Optional<Path> exportToTemp(SavegameEntry<T, I> entry, boolean includeEntryName) {
        return Optional.ofNullable(SavegameContext.mapSavegame(entry, ctx -> {
            var sc = ctx.getStorage();
            var out = FileUtils.getTempDirectory().toPath().resolve(
                    sc.getFileSystemCompatibleName(entry, includeEntryName));
            try {
                sc.copySavegameTo(entry, out);
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                return null;
            }
            return out;
        }));
    }

    public static <T, I extends SavegameInfo<T>> void openSavegame(SavegameEntry<T, I> entry) {
        SavegameContext.withSavegame(entry, ctx -> {
            ThreadHelper.open(ctx.getStorage().getSavegameDataDirectory(entry));
        });
    }

    public static <T, I extends SavegameInfo<T>> void exportSavegame(SavegameEntry<T, I> e) {
        SavegameContext.withSavegame(e, ctx -> {
            try {
                FileExportTarget.createExportTarget(e).export();
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

    public static void loadLatestSavegameCheckpoint(Game g) {
        var savegames = SavegameWatcher.ALL.get(g).getSavegames();
        if (savegames.size() == 0) {
            return;
        }

//        SavegameStorage.get(g).getParser().parse(savegames.get(0).path, RakalyHelper::meltSavegame).visit(
//            new SavegameParseResult.Visitor<>() {
//            @Override
//            public void success(SavegameParseResult.Success<SavegameInfo<?>> s) {
//                var campaignId = s.info.getCampaignHeuristic();
//                SavegameStorage.get(g).getSavegameCollection(campaignId)
//                        .flatMap(col -> col.entryStream().findFirst()).ifPresent(entry -> {
//                    TaskExecutor.getInstance().submitTask(() -> {
//                        SavegameStorage.get(g).loadEntry(entry);
//                        SavegameContext.withSavegame(entry, ctx -> {
//                            GameDistLauncher.continueSavegame(entry, false);
//                        });
//                    }, true);
//                });
//            }
//        });
    }

    public static void importLatestSavegame(Game g) {
        var savegames = SavegameWatcher.ALL.get(g).getSavegames();
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

//        savegames.get(0).importTarget(s -> {
//            s.visit(new SavegameParseResult.Visitor<>() {
//                @Override
//                @SuppressWarnings("unchecked")
//                public void success(SavegameParseResult.Success<SavegameInfo<?>> s) {
//                    SavegameStorage.get(SavegameManagerState.get().current())
//                            .getSavegameForChecksum(s.checksum)
//                            .ifPresent(e -> {
//                                // The info is loaded asynchronously when the savegame is opened in the gui.
//                                // This means that at this point, the info can either be null or not null
//                                // In case it is null, temporarily set it
//                                if (e.infoProperty().get() == null) {
//                                    e.load((SavegameInfo<Object>) s.info);
//                                    GameDistLauncher.continueSavegame(e, false);
//                                    e.unload();
//                                }
//                            });
//                }
//            });
//        });
    }

    public static <T, I extends SavegameInfo<T>> void meltSavegame(SavegameEntry<T, I> e) {
        if (!GuiDialogHelper.showMeltDialog()) {
            return;
        }

//        TaskExecutor.getInstance().submitTask(() -> {
//            SavegameContext.withSavegame(e, ctx -> {
//                Path meltedFile;
//                try {
//                    meltedFile = RakalyHelper.meltSavegame(ctx.getStorage().getSavegameFile(e));
//                } catch (Exception ex) {
//                    ErrorHandler.handleException(ex);
//                    return;
//                }
//                var folder = ctx.getStorage().getOrCreateFolder("Melted savegames");
//                folder.ifPresent(f -> {
//                    ctx.getStorage().importSavegame(meltedFile, null, true, null, f);
//                });
//            });
//        }, true);
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
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                var in = ctx.getStorage().getSavegameFile(e);
                var target = EditTarget.create(in);
                target.ifPresent(t -> {
                    var storageTarget = new StorageEditTarget<>(ctx.getStorage(), e, t);
                    Editor.createNewEditor(storageTarget);
                });
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void reloadSavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                ctx.getStorage().reloadSavegame(e);
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
        }, true);
    }
}