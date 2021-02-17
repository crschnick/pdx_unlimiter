package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SavegameStorageIO {

    public static void exportSavegameStorage(Path out) {
        TaskExecutor.getInstance().submitTask(() -> {
            try {
                FileUtils.forceMkdir(out.toFile());
                for (SavegameStorage<?, ?> cache : SavegameStorage.ALL) {
                    Path cacheDir = out.resolve(cache.getName());
                    Files.createDirectory(cacheDir);
                    exportSavegameDirectory(cache, cacheDir);
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true, true);
    }

    private static <T, I extends SavegameInfo<T>> void exportSavegameDirectory(SavegameStorage<T, I> cache, Path out) throws IOException {
        for (SavegameCollection<T, I> c : cache.getCollections()) {
            for (SavegameEntry<T, I> e : c.getSavegames()) {
                Path fileOut = out.resolve(cache.getFileName(e));
                int counter = 2;
                while (Files.exists(fileOut)) {
                    fileOut = fileOut.resolveSibling("(" + counter + ") " + cache.getFileName(e));
                    counter++;
                }
                cache.copySavegameTo(e, fileOut);
            }
        }
    }
}
