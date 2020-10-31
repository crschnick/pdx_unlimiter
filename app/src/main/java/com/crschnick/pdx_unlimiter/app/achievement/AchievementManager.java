package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonPathConfiguration;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementManager {

    private static AchievementManager INSTANCE;
    private String game;
    private AchievementContent content;
    private String checksum;
    private List<Achievement> achievements = new ArrayList<>();
    public AchievementManager(String game, AchievementContent content) {
        this.game = game;
        this.content = content;
    }

    public static AchievementManager getInstance() {
        return INSTANCE;
    }

    public static void init() throws IOException {
        INSTANCE = new AchievementManager("eu4",
                AchievementContent.EU4);
        INSTANCE.loadData();
        JsonPathConfiguration.init();
    }

    public void refresh() {
        try {
            loadData();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    private void loadData() throws IOException {
        achievements.clear();
        Path official = PdxuInstallation.getInstance().getOfficialAchievementsLocation().resolve(game);
        Files.list(official)
                .filter(p -> p.getFileName().toString().endsWith("json"))
                .forEach(p -> {
                    try {
                        achievements.add(Achievement.fromFile(p, content, true));
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                    }
                });
        Path c = official.resolve("checksum");
        checksum = Files.exists(c) ? Files.readString(c) : "none";

        FileUtils.forceMkdir(PdxuInstallation.getInstance().getUserAchievementsLocation().resolve(game).toFile());
        Files.list(PdxuInstallation.getInstance().getUserAchievementsLocation().resolve(game))
                .filter(p -> p.getFileName().toString().endsWith("json"))
                .forEach(p -> {
                    try {
                        achievements.add(Achievement.fromFile(p, content, false));
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                    }
                });
    }

    private String calculateChecksum() throws IOException {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        MessageDigest finalD = d;
        Path official = PdxuInstallation.getInstance().getOfficialAchievementsLocation().resolve(game);
        Files.list(official)
                .filter(p -> p.getFileName().toString().endsWith("json"))
                .forEach(f -> {
                    try {
                        finalD.update(Files.readAllBytes(f));
                    } catch (IOException e) {
                        ErrorHandler.handleException(e);
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

    public AchievementMatcher validateSavegame(Achievement a, Eu4Campaign.Entry entry) throws IOException {
        INSTANCE.loadData();
        Eu4IntermediateSavegame loaded = Eu4IntermediateSavegame.fromFile(
                SavegameCache.EU4_CACHE.getPath(entry).resolve("data.zip"));

        if (!validateChecksum()) {
            throw new IOException("Wrong achievement checksum");
        }

        return a.match(loaded);
    }

    public boolean validateChecksum() throws IOException {
        return checksum.equals(calculateChecksum());
    }

    public List<Achievement> getSuitableAchievements(Eu4IntermediateSavegame s, boolean onlyOfficial, boolean onlyElgible) {
        return achievements.stream()
                .filter(a -> !onlyOfficial || a.isOfficial())
                .filter(a -> {
                    if (!onlyElgible) {
                        return true;
                    }

                    try {
                        AchievementMatcher m = a.match(s);
                        return m.getValidType().isPresent() && m.getEligibleStatus().isFullfilled();
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}
