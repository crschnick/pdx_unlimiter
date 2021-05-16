package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.util.OsHelper;
import com.crschnick.pdxu.model.SavegameInfo;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SavegameStorageIO {

    public static void exportSavegameStorage(Path out) {
        TaskExecutor.getInstance().submitTask(() -> {
            try {
                FileUtils.forceMkdir(out.toFile());
                for (SavegameStorage<?, ?> storage : SavegameStorage.ALL.values()) {
                    Path storageDir = out.resolve(storage.getName());
                    FileUtils.forceMkdir(storageDir.toFile());
                    exportSavegameDirectory(storage, storageDir);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }

    private static <T, I extends SavegameInfo<T>> void exportSavegameDirectory(SavegameStorage<T, I> storage, Path out) throws IOException {
        for (SavegameCollection<T, I> c : storage.getCollections()) {
            Path colDir = out.resolve(OsHelper.getFileSystemCompatibleName(c.getName() +
                    " (" + c.getUuid().toString().substring(0, 8) + ")"));
            FileUtils.forceMkdir(colDir.toFile());
            for (SavegameEntry<T, I> e : c.getSavegames()) {
                var name = e.getName() + " (" + e.getUuid().toString().substring(0, 8) + ")." +
                        storage.getType().getFileEnding();
                Path fileOut = colDir.resolve(OsHelper.getFileSystemCompatibleName(name));
                if (!Files.exists(fileOut)) {
                    storage.copySavegameTo(e, fileOut);
                }
            }
        }
    }
}
