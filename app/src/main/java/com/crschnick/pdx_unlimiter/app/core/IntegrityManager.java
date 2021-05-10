package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
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

    private final Map<Game,String> checksums = new HashMap<>();

    public static void init() throws Exception {
        INSTANCE = new IntegrityManager();

        Path infoPackage;
        if (PdxuInstallation.getInstance().isImage()) {
            infoPackage = FileSystems.getFileSystem(URI.create("jrt:/")).getPath(
                    "modules",
                    "com.crschnick.pdx_unlimiter.core",
                    "com/crschnick/pdx_unlimiter/core");
        } else {
            var uri = new URI("jar:" + SavegameInfo.class.getProtectionDomain().getCodeSource()
                    .getLocation().toURI().toString());
            infoPackage = FileSystems.newFileSystem(uri, Map.of())
                    .getPath("/com/crschnick/pdx_unlimiter/core");
        }

        for (Game g : Game.values()) {
            INSTANCE.checksums.put(g, calc(infoPackage, g.getId()));
        }
    }

    private static String calc(Path pack, String game) throws Exception {
        MessageDigest d = MessageDigest.getInstance("MD5");
        update(d, pack.resolve("parser"));
        update(d, pack.resolve("info"));
        update(d, pack.resolve("info").resolve(game));

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
