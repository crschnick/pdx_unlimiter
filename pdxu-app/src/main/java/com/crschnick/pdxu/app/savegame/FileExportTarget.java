package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

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

    public abstract Path export() throws Exception;

    public static class StandardExportTarget<T, I extends SavegameInfo<T>> extends FileExportTarget<T, I> {

        public StandardExportTarget(Path savegameDir, SavegameStorage<T, I> storage, SavegameEntry<T, I> entry) {
            super(savegameDir, storage, entry);
        }

        @Override
        public Path export() throws Exception {
            var customId = storage.getCustomCampaignId(entry);
            // Only try to add id suffix in case for ironman or binary ones
            var suffix = entry.getInfo().isIronman() || entry.getInfo().isBinary() ?
                    customId.map(u -> " (" + u + ")").orElse(null) : null;
            var baseName = storage.getValidOutputFileName(entry, false, suffix);
            var out = savegameDir.resolve(baseName);
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
        public Path export() throws Exception {
            var customId = storage.getCustomCampaignId(entry);
            // Only try to add id suffix in case for ironman or binary ones
            var suffix = entry.getInfo().isIronman() || entry.getInfo().isBinary() ?
                    customId.map(u -> " (" + u + ")").orElse(null) : null;
            var baseName = FilenameUtils.getBaseName(
                    storage.getValidOutputFileName(entry, false, suffix).toString());

            Path file;
            Path dir = savegameDir.resolve(baseName);
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
