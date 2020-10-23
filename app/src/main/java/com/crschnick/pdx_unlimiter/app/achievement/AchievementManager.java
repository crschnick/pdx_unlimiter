package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementManager {

    private static AchievementManager INSTANCE;

    public static AchievementManager getInstance() {
        return INSTANCE;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    private static String calculateChecksum() {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        MessageDigest finalD = d;
        Arrays.stream(PdxuInstallation.getInstance().getAchievementsLocation().toFile().listFiles())
                .filter(f -> f.getName().endsWith("json"))
                .forEach(f -> {
                    try {
                        finalD.update(Files.readAllBytes(f.toPath()));
                    } catch (IOException e) {
                        ErrorHandler.handleException(e, false);
                    }
                });
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        return c.toString();
    }

    public static void init() throws IOException {
        INSTANCE = new AchievementManager();
        INSTANCE.loadData();
        JsonPathConfiguration.init();
    }

    private String checksum;
    private List<Achievement> achievements = new ArrayList<>();

    private void loadData() throws IOException {
        achievements.clear();
        Arrays.stream(PdxuInstallation.getInstance().getAchievementsLocation().toFile().listFiles())
                .filter(f -> f.getName().endsWith("json"))
                .forEach(f -> {
                    try {
                        achievements.add(Achievement.fromFile(f.toPath()));
                    } catch (IOException e) {
                        ErrorHandler.handleException(e, false);
                    }
                });

        String s = calculateChecksum();
        checksum = Files.readString(PdxuInstallation.getInstance().getAchievementsLocation().resolve("checksum"));

    }

    public Achievement.Matcher validateSavegame(Achievement a, Eu4Campaign.Entry entry) throws IOException {
        INSTANCE.loadData();
        Eu4IntermediateSavegame loaded = Eu4IntermediateSavegame.fromFile(
                SavegameCache.EU4_CACHE.getPath(entry).resolve("data.zip"));

        if (!validateChecksum()) {
            throw new IOException("Wrong achievement checksum");
        }

        return a.match(loaded);
    }

    public boolean validateChecksum() {
        return checksum.equals(calculateChecksum());
    }

    public List<Achievement> getEligibleAchievements(Eu4IntermediateSavegame s) {
        return achievements.stream()
                .filter(a -> a.match(s).getEligibleStatus().isFullfilled())
                .collect(Collectors.toList());
    }
}
