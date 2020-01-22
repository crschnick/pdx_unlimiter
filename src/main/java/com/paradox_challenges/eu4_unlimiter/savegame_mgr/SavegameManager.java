package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import com.paradox_challenges.eu4_unlimiter.parser.GameDate;
import com.paradox_challenges.eu4_unlimiter.parser.GameTag;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SavegameManager {

    public static class SavegameData {

        public static class War {

            public List<String> enemies;
        }

        private String startTag;
        private String currentTag;
        private GameDate date;
        private String name;
        private List<String> vassals;
        private Map<String, GameDate> truces;
        private List<War> wars;

        public static SavegameData parse(Path p) {
            return null;
        }

        public SavegameData(String startTag, String currentTag, GameDate date, String name) {
            this.startTag = startTag;
            this.currentTag = currentTag;
            this.date = date;
            this.name = name;
        }

        public String getStartTag() {
            return startTag;
        }

        public String getCurrentTag() {
            return currentTag;
        }

        public GameDate getDate() {
            return date;
        }

        public String getName() {
            return name;
        }

        //DLCs
    }

    public static class Campaign {

        private GameTag startTag;
        private List<SavegameData> savegames;
        private String campaignId;
        private boolean ironman;

        public static Campaign parse(Path p) {
            return null;
        }
    }

    public class Settings {

    }

    public static List<Campaign> parseCampaigns(Path p) {
        List<Campaign> list = new ArrayList<>();
        for (File f : p.toFile().listFiles()) {
            list.add(Campaign.parse(f.toPath()));
        }
        return list;
    }
}
