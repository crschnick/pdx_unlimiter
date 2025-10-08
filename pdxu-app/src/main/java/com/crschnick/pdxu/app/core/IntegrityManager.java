package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import io.xpipe.modulefs.ModuleFileSystem;
import lombok.SneakyThrows;

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

    private static IntegrityManager INSTANCE;

    private final Map<Game, String> checksums = new HashMap<>();

    private static ModuleFileSystem openFileSystemIfNeeded(String module) throws IOException {
        var layer = ModuleLayer.boot();
        var fs = (ModuleFileSystem) FileSystems.newFileSystem(URI.create("module:/" + module), Map.of("layer", layer));
        return fs;
    }

    private static Path getPackagePath(String module, String file) throws IOException {
        var fs = openFileSystemIfNeeded(module);
        var f = fs.getPath(file);
        return f;
    }

    @SneakyThrows
    public static void init() {
        INSTANCE = new IntegrityManager();

        var modelPackage = getPackagePath("com.crschnick.pdxu.model", "com/crschnick/pdxu/model");
        var ioPackage = getPackagePath("com.crschnick.pdxu.io", "com/crschnick/pdxu/io");
        var infoPackage = getPackagePath("com.crschnick.pdxu.app", "com/crschnick/pdxu/app/info");
        for (Game g : Game.values()) {
            INSTANCE.checksums.put(g, calc(ioPackage, modelPackage, infoPackage, g.getId()));
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
            var exec = AppInstallation.ofCurrent().getRakalyExecutable();
            if (Files.exists(exec)) {
                d.update(Files.readAllBytes(exec));
            }
        } catch (IOException ex) {
            ErrorEventFactory.fromThrowable(ex).handle();
        }
        return checksum(d);
    }

    private static void update(MessageDigest d, Path pack) throws IOException {
        try (var s = Files.list(pack)) {
            s.filter(Files::isRegularFile).forEach(p -> {
                try {
                    d.update(Files.readAllBytes(p));
                } catch (IOException e) {
                    ErrorEventFactory.fromThrowable(e).handle();
                }
            });
        }
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
