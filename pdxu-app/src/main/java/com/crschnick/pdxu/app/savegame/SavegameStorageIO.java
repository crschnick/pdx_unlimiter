package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.model.SavegameInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SavegameStorageIO {

    public static void exportSavegameStorage(Path out) {
        TaskExecutor.getInstance().submitTask(() -> {
            try {
                FileUtils.forceMkdir(out.toFile());
                for (SavegameStorage<?, ?> cache : SavegameStorage.ALL.values()) {
                    Path cacheDir = out.resolve(cache.getName());
                    FileUtils.forceMkdir(cacheDir.toFile());
                    exportSavegameDirectory(cache, cacheDir);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }

    private static <T, I extends SavegameInfo<T>> void exportSavegameDirectory(SavegameStorage<T, I> cache, Path out) throws IOException {
        for (SavegameCollection<T, I> c : cache.getCollections()) {
            for (SavegameEntry<T, I> e : c.getSavegames()) {
                Path fileOut = out.resolve(cache.getFileSystemCompatibleName(e, true));
                int counter = 0;
                while (Files.exists(fileOut)) {
                    var name = cache.getFileSystemCompatibleName(e, true);
                    fileOut = fileOut.resolveSibling(FilenameUtils.getBaseName(name) +
                            (counter != 0 ? "(" + counter + ") " : "") + "." + FilenameUtils.getExtension(name));
                    counter++;
                }
                cache.copySavegameTo(e, fileOut);
            }
        }
    }
}
