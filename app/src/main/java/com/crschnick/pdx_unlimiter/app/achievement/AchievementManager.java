package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.util.JsonPathConfiguration;
import com.crschnick.pdx_unlimiter.core.savegame.Savegame;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AchievementManager {

    public static AchievementManager EU4;
    public static AchievementManager HOI4;
    public static AchievementManager STELLARIS;
    public static AchievementManager CK3;

    private String game;
    private AchievementContent content;
    private String checksum;
    private List<Achievement> achievements = new ArrayList<>();

    public AchievementManager(String game, AchievementContent content) {
        this.game = game;
        this.content = content;
    }

    public static void init() throws IOException {
        EU4 = new AchievementManager("eu4",
                AchievementContent.EU4);
        EU4.loadData();

        HOI4 = new AchievementManager("hoi4",
                AchievementContent.HOI4);
        HOI4.loadData();

        STELLARIS = new AchievementManager("stellaris",
                AchievementContent.HOI4);
        STELLARIS.loadData();

        CK3 = new AchievementManager("ck3",
                AchievementContent.HOI4);
        CK3.loadData();

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
        FileUtils.forceMkdir(official.toFile());

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

    public <S extends Savegame> Optional<AchievementMatcher> validateSavegame(Achievement a, GameCampaignEntry<?,?> entry) {
        try {
            if (!validateChecksum()) {
                throw new IOException("Wrong achievement checksum");
            }
            loadData();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }

        return a.match(entry);
    }

    public boolean validateChecksum() throws IOException {
        return checksum.equals(calculateChecksum());
    }

    public List<Achievement> getSuitableAchievements(GameCampaignEntry<?,?> entry, boolean onlyOfficial, boolean onlyElgible) {
        return achievements.stream()
                .filter(a -> !onlyOfficial || a.isOfficial())
                .filter(a -> {
                    if (!onlyElgible) {
                        return true;
                    }

                    try {
                        Optional<AchievementMatcher> m = a.match(entry);
                        return m.isPresent() && m.get().getValidType().isPresent() && m.get().getEligibleStatus().isFullfilled();
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }
}
