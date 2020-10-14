package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveHelper {

    public static void importSavegameCache(Path in) throws IOException {
        ZipFile zipFile = new ZipFile(in.toFile());
        String config = SavegameCache.FILE.relativize(SavegameCache.ROOT_DIR).toString();
                    ZipEntry e = zipFile.getEntry(config);
                    SavegameCache.importDataFromConfig(zipFile.getInputStream(e));

                    zipFile.stream().filter(en -> !en.getName().equals(config)).forEach(en -> {
                        try {
                            Path p = SavegameCache.EU4_CACHE.getPath().relativize(Paths.get(en.getName()));
                            if (p.toFile().exists()) {
                                return;
                            }
                            FileUtils.copyToFile(zipFile.getInputStream(en), p.toFile());
                        } catch (IOException fileNotFoundException) {
                            fileNotFoundException.printStackTrace();
                        }
                    });


        zipFile.close();
    }

    public static void exportSavegameDirectory(Path out) throws IOException {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        for (SavegameCache cache : SavegameCache.CACHES) {
            ZipEntry entry = new ZipEntry(cache.getPath().relativize(SavegameCache.ROOT_DIR).resolve("campaigns.json").toString());
            zipFile.putNextEntry(entry);
            SavegameCache.exportDataToConfig(zipFile);
            Set<String> names = new HashSet<>();
            for (Eu4Campaign c : cache.getCampaigns()) {
                for (Eu4Campaign.Entry e : c.getSavegames()) {
                    String name = cache.getEntryName(e);
                    if (names.contains(name)) {
                        name += "_" + UUID.randomUUID().toString();
                    }
                    names.add(name);
                    compressFileToZipfile(
                            cache.getPath(e).toFile(),
                            (cache.getPath().relativize(SavegameCache.ROOT_DIR)).resolve(name + ".eu4").toString(),
                            zipFile);
                }
            }
        }
        zipFile.close();
    }

    public static void exportSavegameCache(Path out) throws IOException {
        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(out.toString()));
        for (SavegameCache cache : SavegameCache.CACHES) {
            for (Eu4Campaign c : cache.getCampaigns()) {
                for (Eu4Campaign.Entry e : c.getSavegames()) {
                    Path file = cache.getPath(e).resolve("savegame.eu4");
                    String name = file.relativize(SavegameCache.ROOT_DIR).toString();
                    compressFileToZipfile(file.toFile(), name, zipFile);
                }
            }
        }
        zipFile.close();
    }

    private static void compressFileToZipfile(File file, String name, ZipOutputStream out) throws IOException {
                ZipEntry entry = new ZipEntry(name);
                out.putNextEntry(entry);

                FileInputStream in = new FileInputStream(file);
                IOUtils.copy(in, out);
                in.close();

    }
}
