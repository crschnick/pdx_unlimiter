package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.util.OsHelper;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        var writtenCollections = new ArrayList<String>();
        for (SavegameCampaign<T, I> c : storage.getCollections()) {
            var colName = getUniqueName(OsHelper.getFileSystemCompatibleName(c.getName()), writtenCollections);
            writtenCollections.add(colName);

            Path colDir = out.resolve(colName);
            FileUtils.forceMkdir(colDir.toFile());
            var writtenEntries = new ArrayList<String>();
            for (SavegameEntry<T, I> e : c.entryStream().toList()) {
                var branchSuffix = " (" + c.getUuid() + ")";
                var eName = OsHelper.getFileSystemCompatibleName(e.getName());
                var outName = getUniqueName(eName, writtenEntries) + branchSuffix +
                        "." + storage.getType().getFileEnding();
                writtenEntries.add(eName);

                Path fileOut = colDir.resolve(outName);
                storage.copySavegameTo(e, fileOut);
            }
        }
    }

    private static String getUniqueName(String start, List<String> written) {
        if (!written.contains(start)) {
            return start;
        }

        int counter = 1;
        while (true) {
            var newName = start + "(" + counter + ")";
            if (!written.contains(newName)) {
                return newName;
            }
            counter++;
        }
    }
}
