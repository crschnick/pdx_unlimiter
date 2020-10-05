package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import com.paradox_challenges.eu4_unlimiter.parser.GameDate;
import com.paradox_challenges.eu4_unlimiter.parser.GameTag;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4IntermediateSavegame;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4Savegame;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Eu4Campaign {

    public static class Entry {

        public static class War {

            public List<String> enemies;
        }

        private String startTag;
        private String currentTag;
        private GameDate date;
        private List<String> vassals;
        private Map<String, GameDate> truces;
        private List<SavegameManager.SavegameData.War> wars;

        public static Entry fromSavegame(Eu4IntermediateSavegame save) {
            return new Entry(null, null, null);
        }

        public static Entry parse(Path p) {
            String last = p.getName(p.getNameCount() - 1).toString();
            UUID id = UUID.fromString(last);
            return null;
        }

        public Entry(String startTag, String currentTag, GameDate date) {
            this.startTag = startTag;
            this.currentTag = currentTag;
            this.date = date;
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

        //DLCs
    }

    private UUID campaignId;
    private List<Entry> savegames;

    public Eu4Campaign(UUID campaignId, List<Entry> savegames) {
        this.campaignId = campaignId;
        this.savegames = savegames;
    }

    public static Eu4Campaign parse(Path p) {
        String last = p.getName(p.getNameCount() - 1).toString();
        UUID id = UUID.fromString(last);

        File dir = p.toFile();
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        List<Entry> savegames = new ArrayList<>();
        for (File f : dir.listFiles(fileFilter)) {
            Entry entry = Entry.parse(f.toPath());
            savegames.add(entry);
        }
        return new Eu4Campaign(id, savegames);
    }

    public void add(Entry e) {
        this.savegames.add(e);
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public List<Entry> getSavegames() {
        return savegames;
    }
}
