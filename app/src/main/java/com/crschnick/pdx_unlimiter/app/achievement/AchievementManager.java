package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AchievementManager {

    private static AchievementManager INSTANCE;

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
        return d.toString();
    }

    public static void init() throws IOException {
        var list = new ArrayList<Achievement>();
        Arrays.stream(PdxuInstallation.getInstance().getAchievementsLocation().toFile().listFiles())
                .filter(f -> f.getName().endsWith("json"))
                .forEach(f -> {
                    try {
                        list.add(Achievement.fromFile(f.toPath()));
                    } catch (IOException e) {
                        ErrorHandler.handleException(e, false);
                    }
                });
        
        String c = Files.readString(PdxuInstallation.getInstance().getAchievementsLocation().resolve("checksum"));
        INSTANCE = new AchievementManager(c, list);
        JsonPathConfiguration.init();


        Eu4IntermediateSavegame i = null;
        try {
            i = Eu4IntermediateSavegame.fromFile(
                    Path.of("C:\\Users\\cschn\\pdx_unlimiter\\savegames\\eu4\\5b4e1af0-27f0-4fe2-b78b-233c782ba7f0\\262b7401-1a8c-4255-88b5-089739bb8211\\data.zip"));

            Achievement a = Achievement.fromFile(PdxuInstallation.getInstance().getAchievementsLocation().resolve("ach.json"));
            String s = a.getReadableScore();
            a.score(i);
            Eu4IntermediateSavegame finalI = i;
            Eu4IntermediateSavegame finalI1 = i;
            AchievementWindow.showAchievementDialog(a, i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String checksum;
    private List<Achievement> achievements;

    public AchievementManager(String checksum, List<Achievement> achievements) {
        this.checksum = checksum;
        this.achievements = achievements;
    }

    public boolean validateChecksum() {
        return checksum.equals(calculateChecksum());
    }

    public List<Achievement> getEligibleAchievements(Eu4IntermediateSavegame s) {
        return achievements.stream()
                .filter(a -> a.checkAchieved(s).isFullfilled())
                .collect(Collectors.toList());
    }
}
