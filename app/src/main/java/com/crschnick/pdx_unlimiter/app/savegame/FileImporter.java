package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.game.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.FileWatchManager;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class FileImporter {

    private static final Logger logger = LoggerFactory.getLogger(FileImporter.class);

    private static FileImporter INSTANCE;

    public static void init() throws IOException {
        INSTANCE = new FileImporter();
        var path = PdxuInstallation.getInstance().getImportQueueLocation();
        FileUtils.forceMkdir(path.toFile());

        Consumer<Path> importFunc = p -> {
            if (!Files.exists(p)) {
                return;
            }
            try {
                String toImport = Files.readString(p);
                importFileInternal(p, toImport);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        };

        Files.list(path).forEach(importFunc);
        FileWatchManager.getInstance().startWatchersInDirectories(List.of(path), importFunc);
    }

    private static void importFileInternal(Path queueFile, String input) {
        logger.debug("Starting to import " + input + " from queue file " + queueFile);
        var targets = FileImportTarget.createTargets(input);
        if (targets.size() == 0) {
            logger.debug("No targets to import");
        } else {
            for (FileImportTarget t : targets) {
                logger.debug("Starting to import target " + t.getName() + " from " + input);
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

    public static void addToImportQueue(String toImport) {
        try {
            FileUtils.forceMkdir(PdxuInstallation.getInstance().getImportQueueLocation().toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        var path = PdxuInstallation.getInstance().getImportQueueLocation()
                .resolve(UUID.randomUUID().toString());

        logger.debug("Creating queue file at " + path + " for import target " + toImport);
        try {
            Files.writeString(path, toImport);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

    }
}
