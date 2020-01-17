package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import com.paradox_challenges.eu4_unlimiter.parser.GameDate;
import com.paradox_challenges.eu4_unlimiter.parser.GameTag;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SavegameManager {

    public static class SavegameData {

        private GameTag startTag;
        private GameTag currentTag;
        private GameDate date;
        private String name;

        public static SavegameData parse(Path p) {
            return null;
        }

        public SavegameData(GameTag startTag, GameTag currentTag, GameDate date, String name) {
            this.startTag = startTag;
            this.currentTag = currentTag;
            this.date = date;
            this.name = name;
        }

        public GameTag getStartTag() {
            return startTag;
        }

        public GameTag getCurrentTag() {
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
