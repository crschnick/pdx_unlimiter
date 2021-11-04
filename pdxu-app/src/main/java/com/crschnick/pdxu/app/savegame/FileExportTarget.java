package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

public abstract class FileExportTarget<T, I extends SavegameInfo<T>> {

    protected final Path targetDir;
    protected final boolean includeEntryName;
    protected SavegameStorage<T, I> storage;
    protected SavegameEntry<T, I> entry;

    FileExportTarget(Path targetDir, boolean includeEntryName, SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
        this.targetDir = targetDir;
        this.includeEntryName = includeEntryName;
        this.storage = storage;
        this.entry = entry;
    }

    public static <T, I extends SavegameInfo<T>> FileExportTarget<T, I> createExportTarget(SavegameEntry<T, I> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            return createExportTarget(ctx.getInstallation().getSavegamesDir(), false, entry);
        });
    }

    @SuppressWarnings("unchecked")
    public static <T, I extends SavegameInfo<T>> FileExportTarget<T, I> createExportTarget(Path dir, boolean includeEntryName, SavegameEntry<T, I> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            if (SavegameStorage.get(Game.STELLARIS).equals(ctx.getStorage())) {
                return (FileExportTarget<T, I>) new StellarisExportTarget(
                        dir,
                        includeEntryName,
                        (SavegameStorage<StellarisTag, StellarisSavegameInfo>) ctx.getStorage(),
                        (SavegameEntry<StellarisTag, StellarisSavegameInfo>) entry);
            } else {
                return new StandardExportTarget<>(dir, includeEntryName, ctx.getStorage(), entry);
            }
        });
    }

    public abstract Path export() throws Exception;

    public static class StandardExportTarget<T, I extends SavegameInfo<T>> extends FileExportTarget<T, I> {

        public StandardExportTarget(Path savegameDir, boolean includeEntryName, SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
            super(savegameDir, includeEntryName, storage, entry);
        }

        private Path getOutputFile() {
            var customId = storage.getCustomCampaignId(entry);
            // Only try to add id suffix in case for ironman or binary ones
            var suffix = entry.getInfo().isIronman() || entry.getInfo().isBinary() ?
                    customId.map(u -> " (" + u + ")").orElse(null) : null;
            var baseName = storage.getValidOutputFileName(entry, includeEntryName, suffix);
            return targetDir.resolve(baseName);
        }

        @Override
        public Path export() throws Exception {
            var out = getOutputFile();
            storage.copySavegameTo(entry, out);
            return out;
        }
    }

    public static class StellarisExportTarget extends FileExportTarget<StellarisTag, StellarisSavegameInfo> {

        public StellarisExportTarget(
                Path savegameDir,
                boolean includeEntryName,
                SavegameStorage<StellarisTag, StellarisSavegameInfo> storage,
                SavegameEntry<StellarisTag, StellarisSavegameInfo> entry) {
            super(savegameDir, includeEntryName, storage, entry);
        }

        @Override
        public Path export() throws Exception {
            var baseName = FilenameUtils.getBaseName(
                    storage.getValidOutputFileName(entry, includeEntryName, null).toString());

            Path file;
            Path dir = savegameDir.resolve(FilenameUtils.getBaseName(
                    storage.getCompatibleName(entry, false)));
            if (entry.getInfo().getData().isIronman()) {
                file = dir.resolve("ironman.sav");
            } else {
                file = dir.resolve(entry.getDate().toString() + ".sav");
            }
            storage.copySavegameTo(entry, file);
            return file;
        }
    }
}
