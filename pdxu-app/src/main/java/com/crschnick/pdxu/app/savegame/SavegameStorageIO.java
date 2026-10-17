package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.DesktopHelper;
import com.crschnick.pdxu.app.util.FileSystemHelper;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SavegameStorageIO {

    public static void exportSavegameStorage(Path out) {
        TaskExecutor.getInstance()
                .submitTask(
                        () -> {
                            try {
                                FileUtils.forceMkdir(out.toFile());
                                for (SavegameStorage<?, ?> storage : SavegameStorage.ALL.values()) {
                                    Path storageDir = out.resolve(storage.getName());
                                    FileUtils.forceMkdir(storageDir.toFile());
                                    exportSavegameDirectory(storage, storageDir);
                                }
                                DesktopHelper.browseFile(out);
                            } catch (Exception e) {
                                ErrorEventFactory.fromThrowable(e).handle();
                            }
                        },
                        true);
    }

    private static <T, I extends SavegameInfo<T>> void exportSavegameDirectory(SavegameStorage<T, I> storage, Path out)
            throws IOException {
        var writtenCollections = new HashSet<String>();
        for (SavegameCampaign<T, I> c : storage.getCollections()) {
            var colName = getUniqueName(FileSystemHelper.getFileSystemCompatibleName(c.getName()), writtenCollections);

            Path colDir = out.resolve(colName);
            FileUtils.forceMkdir(colDir.toFile());
            var writtenEntries = new HashSet<String>();
            for (SavegameEntry<T, I> e : c.entryStream().toList()) {
                var branchSuffix = " (" + c.getUuid() + ")";
                var eName = FileSystemHelper.getFileSystemCompatibleName(e.getName());
                var outName = getUniqueName(eName, writtenEntries) + branchSuffix + "."
                        + storage.getType().getFileEnding();

                Path fileOut = colDir.resolve(outName);
                storage.copySavegameTo(e, fileOut);
            }
        }
    }

    private static String getUniqueName(String start, Set<String> written) {
        var name = start;
        int counter = 1;
        while (!written.add(name.toLowerCase(Locale.ROOT))) {
            name = start + "(" + counter + ")";
            counter++;
        }
        return name;
    }
}
