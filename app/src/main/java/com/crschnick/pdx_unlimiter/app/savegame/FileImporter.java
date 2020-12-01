package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.util.WatcherHelper;
import com.crschnick.pdx_unlimiter.eu4.io.SavegameWriter;
import com.crschnick.pdx_unlimiter.eu4.savegame.Ck3RawSavegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Ck3Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.RawSavegameVisitor;
import com.crschnick.pdx_unlimiter.eu4.savegame.StellarisRawSavegame;
import org.apache.commons.io.FileUtils;

import java.io.FileOutputStream;
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
        WatcherHelper.startWatcherInDirectory("Importer", path,
                p -> {
                    try {
                        String toImport = Files.readString(p);
                        importFileInternal(p, Path.of(toImport));
                    } catch (IOException e) {
                        ErrorHandler.handleException(e);
                    }
                }, StandardWatchEventKinds.ENTRY_CREATE);
    }

    private static void importFileInternal(Path queueFile, Path p) throws IOException {
        RawSavegameVisitor.vist(p, new RawSavegameVisitor() {
            @Override
            public void visitEu4(Path file) {
                SavegameCache.EU4_CACHE.importSavegame(p);
            }

            @Override
            public void visitHoi4(Path file) {
                SavegameCache.HOI4_CACHE.importSavegame(p);
            }

            @Override
            public void visitStellaris(Path file) {
                SavegameCache.STELLARIS_CACHE.importSavegame(p);
            }

            @Override
            public void visitCk3(Path file) {
                try {
                    var s = Ck3RawSavegame.fromFile(file);
                    var ss = Ck3Savegame.fromSavegame(s);
                    ss.write(Path.of("C:\\Users\\cschn\\Desktop\\pdx_test\\ck3.zip"), true);
      } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void visitOther(Path file) {
            }
        });
        FileUtils.forceDelete(queueFile.toFile());
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

        addToImportQueue(List.of(savegames.get(0)));
    }
}
