package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class IntegrityManager {

    private static final Logger logger = LoggerFactory.getLogger(IntegrityManager.class);
    private static IntegrityManager INSTANCE;

    private final Map<Game, String> checksums = new HashMap<>();

    public static void init() throws Exception {
        INSTANCE = new IntegrityManager();

        Path modelPackage;
        Path ioPackage;
        Path infoPackage;
        if (PdxuInstallation.getInstance().isImage()) {
            modelPackage = FileSystems.getFileSystem(URI.create("jrt:/")).getPath(
                    "modules",
                    "com.crschnick.pdxu.model",
                    "com/crschnick/pdxu/model");
            ioPackage = FileSystems.getFileSystem(URI.create("jrt:/")).getPath(
                    "modules",
                    "com.crschnick.pdxu.io",
                    "com/crschnick/pdxu/io");
            infoPackage = FileSystems.getFileSystem(URI.create("jrt:/")).getPath(
                    "modules",
                    "com.crschnick.pdxu.app",
                    "com/crschnick/pdxu/app/info");
            for (Game g : Game.values()) {
                INSTANCE.checksums.put(g, calc(ioPackage, modelPackage, infoPackage, g.getId()));
            }
        } else {
            var modelUri = new URI("jar:" + GameDate.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().toString());
            var ioUri = new URI("jar:" + SavegameType.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().toString());
            var infoUri = new URI("jar:" + SavegameInfo.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().toString());
            try (var modelFs = FileSystems.newFileSystem(modelUri, Map.of());
                 var ioFs = FileSystems.newFileSystem(ioUri, Map.of());
                 var infoFs = FileSystems.newFileSystem(infoUri, Map.of())) {
                modelPackage = modelFs.getPath("/com/crschnick/pdxu/model");
                ioPackage = ioFs.getPath("/com/crschnick/pdxu/io");
                infoPackage = infoFs.getPath("/com/crschnick/pdxu/app/info");

                for (Game g : Game.values()) {
                    INSTANCE.checksums.put(g, calc(ioPackage, modelPackage, infoPackage, g.getId()));
                }
            }
        }
    }

    private static String calc(Path ioPackage, Path modelPackage, Path infoPackage, String game) throws Exception {
        MessageDigest d = MessageDigest.getInstance("MD5");
        update(d, ioPackage.resolve("parser"));
        update(d, ioPackage.resolve("savegame"));
        update(d, ioPackage.resolve("node"));
        update(d, modelPackage);
        update(d, modelPackage.resolve(game));
        update(d, infoPackage);
        update(d, infoPackage.resolve(game));

        // Rebuild caches if ironman converter changes
        try {
            var exec = PdxuInstallation.getInstance().getRakalyExecutable();
            if (Files.exists(exec)) {
                d.update(Files.readAllBytes(exec));
            }
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }
        return checksum(d);
    }

    private static void update(MessageDigest d, Path pack) throws IOException {
        Files.list(pack).filter(Files::isRegularFile).forEach(p -> {
            try {
                d.update(Files.readAllBytes(p));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    private static String checksum(MessageDigest d) {
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        return c.toString();
    }

    public static IntegrityManager getInstance() {
        return INSTANCE;
    }

    public String getChecksum(Game g) {
        return checksums.get(g);
    }
}
