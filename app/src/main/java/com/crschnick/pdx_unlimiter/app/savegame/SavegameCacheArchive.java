package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class SavegameCacheArchive {

    public static void importSavegameCaches(Path in) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(in.toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        for (SavegameCache cache : SavegameCache.ALL) {
            //cache.importSavegameCache(zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static void exportSavegameCaches(Path out) {
        ZipOutputStream zipFile = null;
        try {
            zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        } catch (FileNotFoundException e) {
            ErrorHandler.handleException(e);
            return;
        }

        for (SavegameCache cache : SavegameCache.ALL) {
            exportSavegameDirectory(cache, zipFile);
        }

        try {
            zipFile.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private static <T, I extends SavegameInfo<T>> void exportSavegameDirectory(SavegameCache<?,?,T,I> cache, ZipOutputStream out) {
        Set<String> names = new HashSet<>();
        for (GameCampaign<T, I> c : cache.getCampaigns()) {
            for (GameCampaignEntry<T, I> e : c.getEntries()) {
                String name = cache.getEntryName(e);
                if (names.contains(name)) {
                    name += "_" + UUID.randomUUID().toString();
                }
                names.add(name);
                try {
                    compressFileToZipfile(
                            cache.getPath(e).resolve("savegame." + name).toFile(),
                            PdxuInstallation.getInstance().getSavegameLocation()
                                    .relativize(cache.getPath()).resolve(name + "." + name).toString(),
                            out);
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException);
                }
            }
        }
    }

    private static void compressFileToZipfile(File file, String name, ZipOutputStream out) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        out.putNextEntry(entry);

        FileInputStream in = new FileInputStream(file);
        IOUtils.copy(in, out);
        in.close();
    }
}
