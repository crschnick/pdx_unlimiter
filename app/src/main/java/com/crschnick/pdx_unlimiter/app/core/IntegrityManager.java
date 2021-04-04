package com.crschnick.pdx_unlimiter.app.core;

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
import java.util.Map;

public class IntegrityManager {

    private static final Logger logger = LoggerFactory.getLogger(IntegrityManager.class);
    private static IntegrityManager INSTANCE;

    private String eu4Checksum;
    private String ck3Checksum;
    private String hoi4Checksum;
    private String stellarisChecksum;

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

        INSTANCE.eu4Checksum = calc(infoPackage, "eu4");
        INSTANCE.ck3Checksum = calc(infoPackage, "ck3");
        INSTANCE.hoi4Checksum = calc(infoPackage, "hoi4");
        INSTANCE.stellarisChecksum = calc(infoPackage, "stellaris");
    }

    private static String calc(Path pack, String game) throws Exception {
        MessageDigest d = MessageDigest.getInstance("MD5");
        update(d, pack.resolve("parser"));
        update(d, pack.resolve("info"));
        update(d, pack.resolve("info").resolve(game));
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

    public String getEu4Checksum() {
        return eu4Checksum;
    }

    public String getCk3Checksum() {
        return ck3Checksum;
    }

    public String getHoi4Checksum() {
        return hoi4Checksum;
    }

    public String getStellarisChecksum() {
        return stellarisChecksum;
    }
}
