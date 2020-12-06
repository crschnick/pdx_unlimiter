package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.WatcherHelper;
import com.crschnick.pdx_unlimiter.eu4.savegame.RawSavegameVisitor;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.List;
import java.util.UUID;

public class FileImporter {

    private static FileImporter INSTANCE;

    public static void init() throws IOException {
        INSTANCE = new FileImporter();
        var path = PdxuInstallation.getInstance().getSavegameLocation().resolve("import");
        FileUtils.cleanDirectory(path.toFile());
        WatcherHelper.startWatchersInDirectories("Importer", List.of(path), p -> {
            if (!Files.exists(p)) {
                return;
            }
            try {
                String toImport = Files.readString(p);
                importFileInternal(p, Path.of(toImport));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    private static void importFileInternal(Path queueFile, Path p) throws IOException {
        var target = FileImportTarget.create(p);
        if (target.isEmpty()) {
            return;
        }

        boolean succ = target.get().importTarget();
        if (succ && Settings.getInstance().deleteOnImport()) {
            target.get().delete();
        }

        Files.delete(queueFile);
    }

    public static void addToImportQueue(List<Path> files) {
        try {
            FileUtils.forceMkdir(PdxuInstallation.getInstance().getSavegameLocation().resolve("import").toFile());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        for (Path p : files) {
            var path = PdxuInstallation.getInstance().getSavegameLocation()
                    .resolve("import")
                    .resolve(UUID.randomUUID().toString());
            try {
                Files.writeString(path, p.toString());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public static void importLatestSavegame() {
        var savegames = GameIntegration.current().getInstallation().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        addToImportQueue(List.of(savegames.get(0).getPath()));
    }
}
