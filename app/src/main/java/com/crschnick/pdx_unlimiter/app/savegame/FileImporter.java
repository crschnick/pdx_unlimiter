package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.FileWatchManager;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.editor.Editor;
import com.crschnick.pdx_unlimiter.app.editor.target.EditTarget;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiImporter;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import javafx.application.Platform;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.*;
import java.util.function.BiConsumer;

public class FileImporter {

    private static final Logger logger = LoggerFactory.getLogger(FileImporter.class);


    public static void init() throws IOException {
        var path = PdxuInstallation.getInstance().getImportQueueLocation();
        FileUtils.forceMkdir(path.toFile());

        BiConsumer<Path, WatchEvent.Kind<Path>> importFunc = (p, k) -> {
            if (!Files.exists(p)) {
                return;
            }

            importFromQueue(p);
        };

        Files.list(path).forEach(FileImporter::importFromQueue);
        FileWatchManager.getInstance().startWatchersInDirectories(List.of(path), importFunc);
    }

    private static void importFromQueue(Path queueFile) {
        String input = null;
        try {
            input = Files.readString(queueFile);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        logger.debug("Starting to import " + input + " from queue file " + queueFile);
        var targets = FileImportTarget.createTargets(input);
        if (targets.size() == 0) {
            logger.debug("No targets to import. Trying editor targets ...");
            EditTarget.create(Path.of(input)).ifPresent(Editor::createNewEditor);

        } else {
            for (FileImportTarget t : targets) {
                logger.debug("Starting to import target " + t.getName() + " from " + input);
                t.importTarget(s -> {
                    if (Settings.getInstance().deleteOnImport.getValue()) {
                        logger.debug("Deleting import target " + t.getName());
                        t.delete();
                    }
                });
            }
        }

        logger.debug("Deleting queue file " + queueFile);
        FileUtils.deleteQuietly(queueFile.toFile());
    }

    public static void importTargets(Collection<FileImportTarget> targets) {
        Map<FileImportTarget, SavegameParser.Status> statusMap = new HashMap<>();
        targets.forEach(t -> t.importTarget(s -> {
            // Only save non success results
            // This is done to gc the success objects and improve memory usage
            if (!(s instanceof SavegameParser.Success)) {
                statusMap.put(t, s);
            }

            if (Settings.getInstance().deleteOnImport.getValue()) {
                logger.debug("Deleting import target " + t.getName());
                t.delete();
            }
        }));
        TaskExecutor.getInstance().submitTask(
                () -> {
                    // Report errors
                    statusMap.entrySet().stream()
                            .filter(e -> e.getValue() instanceof SavegameParser.Error)
                            .findFirst()
                            .ifPresent(e -> {
                                ErrorHandler.handleException(
                                        ((SavegameParser.Error) e.getValue()).error,
                                        null,
                                        e.getKey().getPath());
                            });

                    Platform.runLater(() -> GuiImporter.showResultDialog(statusMap));
                }, false);
    }


    public static void addToLazyImportQueue(Path toImport) {
        try {
            Files.createDirectories(PdxuInstallation.getInstance().getLazyImportStorageLocation());
            var path = PdxuInstallation.getInstance().getLazyImportStorageLocation()
                    .resolve(UUID.randomUUID().toString());
            logger.debug("Copying file to " + path + " for lazy import target " + toImport);
            Files.copy(toImport, path);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
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
