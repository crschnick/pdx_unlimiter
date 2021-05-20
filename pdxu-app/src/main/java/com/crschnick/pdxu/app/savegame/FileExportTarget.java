package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;

public abstract class FileExportTarget<T, I extends SavegameInfo<T>> {

    protected Path savegameDir;
    protected SavegameStorage<T, I> storage;
    protected SavegameEntry<T, I> entry;

    FileExportTarget(Path savegameDir, SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
        this.savegameDir = savegameDir;
        this.storage = storage;
        this.entry = entry;
    }

    @SuppressWarnings("unchecked")
    public static <T, I extends SavegameInfo<T>> FileExportTarget<T, I> createExportTarget(SavegameEntry<T, I> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            if (SavegameStorage.get(Game.STELLARIS).equals(ctx.getStorage())) {
                return (FileExportTarget<T, I>) new StellarisExportTarget(
                        ctx.getInstallation().getSavegamesDir(),
                        (SavegameStorage<StellarisTag, StellarisSavegameInfo>) ctx.getStorage(),
                        (SavegameEntry<StellarisTag, StellarisSavegameInfo>) entry);
            } else {
                return new StandardExportTarget<>(ctx.getInstallation().getSavegamesDir(), ctx.getStorage(), entry);
            }
        });
    }

    public abstract Path export() throws IOException;

    public static class StandardExportTarget<T, I extends SavegameInfo<T>> extends FileExportTarget<T, I> {

        public StandardExportTarget(Path savegameDir, SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
            super(savegameDir, storage, entry);
        }

        @Override
        public Path export() throws IOException {
            var out = savegameDir.resolve(storage.getFileSystemCompatibleName(entry, false));
            storage.copySavegameTo(entry, out);
            return out;
        }
    }

    public static class StellarisExportTarget extends FileExportTarget<StellarisTag, StellarisSavegameInfo> {

        public StellarisExportTarget(
                Path savegameDir,
                SavegameStorage<StellarisTag, StellarisSavegameInfo> storage,
                SavegameEntry<StellarisTag, StellarisSavegameInfo> entry) {
            super(savegameDir, storage, entry);
        }

        @Override
        public Path export() throws IOException {
            Path file;
            Path dir = savegameDir.resolve(FilenameUtils.getBaseName(
                    storage.getFileSystemCompatibleName(entry, false)));
            if (entry.getInfo().isIronman()) {
                file = dir.resolve("ironman.sav");
            } else {
                file = dir.resolve(entry.getDate().toString() + ".sav");
            }
            storage.copySavegameTo(entry, file);
            return file;
        }
    }
}
