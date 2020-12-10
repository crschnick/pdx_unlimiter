package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.WatcherHelper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Consumer;

public class FileImporter {

    private static final Logger logger = LoggerFactory.getLogger(FileImporter.class);

    private static FileImporter INSTANCE;

    private Queue<Path> importQueue = new SynchronousQueue<>();

    public static void init() throws IOException {
        INSTANCE = new FileImporter();
        var path = PdxuInstallation.getInstance().getSavegameLocation().resolve("import");

        Consumer<Path> importFunc = p -> {
            if (!Files.exists(p)) {
                return;
            }
            try {
                String toImport = Files.readString(p);
                importFileInternal(p, Path.of(toImport));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        };

        Files.list(path).forEach(importFunc);
        WatcherHelper.startWatchersInDirectories("Importer", List.of(path), importFunc);
    }

    private static void importFileInternal(Path queueFile, Path p) {
        logger.debug("Starting to import " + p + " from queue file " + queueFile);
        var targets = FileImportTarget.createTargets(p);
        if (targets.size() == 0) {
            logger.debug("No targets to import");
        } else {
            for (FileImportTarget t : targets) {
                logger.debug("Starting to import target " + t.getName() + " with path " + t.getPath());
                t.importTarget(() -> {
                    if (Settings.getInstance().deleteOnImport()) {
                        logger.debug("Deleting import target " + t.getName());
                        t.delete();
                    }
                });
            }
        }

        logger.debug("Deleting queue file " + queueFile);
        FileUtils.deleteQuietly(queueFile.toFile());
    }

    public static void addToImportQueue(Path file) {
        try {
            FileUtils.forceMkdir(PdxuInstallation.getInstance().getSavegameLocation().resolve("import").toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        var path = PdxuInstallation.getInstance().getSavegameLocation()
                .resolve("import")
                .resolve(UUID.randomUUID().toString());

        logger.debug("Creating queue file at " + path + " for import file " + file);
        try {
            Files.writeString(path, file.toString());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

    }

    public static void importLatestSavegame() {
        var savegames = GameIntegration.current().getInstallation().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        addToImportQueue(savegames.get(0).getPath());
    }
}
