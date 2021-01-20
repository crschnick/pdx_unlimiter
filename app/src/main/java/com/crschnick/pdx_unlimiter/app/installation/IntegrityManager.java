package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class IntegrityManager {

    private static final Logger logger = LoggerFactory.getLogger(IntegrityManager.class);
    private static IntegrityManager INSTANCE;
    private static final String[] DATA_COMPONENTS = new String[] {
            "Ck3Tag", "Eu4Tag", "GameDate", "GameDateType", "GameVersion", "Hoi4Tag", "StellarisTag"};
    private static final String[] SAVEGAME_COMPONENTS = new String[] {
            "Ck3SavegameInfo", "Eu4SavegameInfo", "Hoi4SavegameInfo", "StellarisSavegameInfo", "SavegameInfo"};

    private String coreChecksum = "none";

    public static void init() throws Exception {
        INSTANCE = new IntegrityManager();

        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 missing!");
        }

        for (var s : DATA_COMPONENTS) {
            d.update(GameVersion.class.getResourceAsStream(s + ".class").readAllBytes());
        }
        for (var s : SAVEGAME_COMPONENTS) {
            d.update(SavegameInfo.class.getResourceAsStream(s + ".class").readAllBytes());
        }

        INSTANCE.coreChecksum = checksum(d);
        logger.debug("Core checksum: " + INSTANCE.coreChecksum);
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

    public String getCoreChecksum() {
        return coreChecksum;
    }
}
