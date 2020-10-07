package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Eu4Campaign {

    public static class Entry {

        public static class Ruler {

            private String name;
            private int adm;
            private int dip;
            private int mil;

            public Ruler(String name, int adm, int dip, int mil) {
                this.name = name;
                this.adm = adm;
                this.dip = dip;
                this.mil = mil;
            }

            public String getName() {
                return name;
            }

            public int getAdm() {
                return adm;
            }

            public int getDip() {
                return dip;
            }

            public int getMil() {
                return mil;
            }

            public static Optional<Ruler> fromCountryNode(Node n, String type) {
                Optional<Ruler> current = Optional.empty();
                for (Node e : Node.getNodeArray(Node.getNodeForKey(Node.getNodeForKey(n, "history"), "events"))) {
                    if (Node.hasKey(e, type)) {
                        Node r = Node.getNodeForKey(e, type);
                        current = Optional.of(new Ruler(Node.getString(Node.getNodeForKey(r, "name")),
                                Node.getInteger(Node.getNodeForKey(r, "ADM")),
                                Node.getInteger(Node.getNodeForKey(r, "DIP")),
                                Node.getInteger(Node.getNodeForKey(r, "MIL"))));
                    }
                }
                return current;
            }
        }

        public static class Version {
            private int first;
            private int second;
            private int third;
            private int fourth;

            public Version(int first, int second, int third, int fourth) {
                this.first = first;
                this.second = second;
                this.third = third;
                this.fourth = fourth;
            }

            @Override
            public String toString() {
                return first + "." + second + "." + third + "." + fourth;
            }

            public int getFirst() {
                return first;
            }

            public int getSecond() {
                return second;
            }

            public int getThird() {
                return third;
            }

            public int getFourth() {
                return fourth;
            }
        }

        public static class War {

            private String title;
            private int score;
            private Set<String> enemies;

            public String getTitle() {
                return title;
            }

            public int getScore() {
                return score;
            }

            public Set<String> getEnemies() {
                return enemies;
            }
        }

        private String currentTag;
        private GameDate date;
        private Version version;
        private Ruler ruler;
        private Optional<Ruler> heir;
        private Set<String> vassals = new HashSet<>();
        private Set<String> allies = new HashSet<>();
        private Set<String> marches = new HashSet<>();
        private Set<String> marriages = new HashSet<>();
        private Set<String> guarantees = new HashSet<>();
        private Optional<String> overlord = Optional.empty();
        private Set<String> juniorPartners = new HashSet<>();
        private Optional<String> seniorPartner = Optional.empty();
        private Set<String> tributaryJuniors = new HashSet<>();
        private Optional<String> tributarySenior = Optional.empty();
        private Map<String, GameDate> truces = new HashMap<>();
        private Set<War> wars = new HashSet<>();;
        private UUID saveId;

        public static Entry fromSavegame(Eu4IntermediateSavegame save, UUID saveId) {
            GameDate date = GameDate.fromNode(Node.getNodeForKey(save.getNodes().get("meta"), "date"));
            String tag = Node.getString(Node.getNodeForKey(save.getNodes().get("meta"), "player"));
            Entry e = new Entry();
            e.saveId = saveId;
            e.date = date;
            e.currentTag = tag;
            e.ruler = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries"), tag), "monarch").get();
            e.heir = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries"), tag), "heir");
            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "dependencies"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal"))  {
                        e.vassals.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal"))  {
                        e.vassals.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union"))  {
                        e.juniorPartners.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state"))  {
                        e.tributaryJuniors.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                }

                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal"))  {
                        e.overlord = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal"))  {
                        e.overlord = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union"))  {
                        e.seniorPartner = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state"))  {
                        e.tributarySenior = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                }

            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "alliances"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.allies.add(Node.getString(Node.getNodeForKey(n, "second")));
                }
                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    e.allies.add(Node.getString(Node.getNodeForKey(n, "first")));
                }
            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "royal_marriages"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.marriages.add(Node.getString(Node.getNodeForKey(n, "second")));
                }
                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    e.marriages.add(Node.getString(Node.getNodeForKey(n, "first")));
                }
            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "guarantees"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.guarantees.add(Node.getString(Node.getNodeForKey(n, "second")));
                }
            }


            Node v = Node.getNodeForKey(save.getNodes().get("meta"), "savegame_version");
            e.version = new Version(Node.getInteger(Node.getNodeForKey(v, "first")),
                    Node.getInteger(Node.getNodeForKey(v, "second")),
                    Node.getInteger(Node.getNodeForKey(v, "third")),
                    Node.getInteger(Node.getNodeForKey(v, "forth")));

            return e;
        }

        public Ruler getRuler() {
            return ruler;
        }

        public Optional<Ruler> getHeir() {
            return heir;
        }

        public Version getVersion() {
            return version;
        }

        public String getCurrentTag() {
            return currentTag;
        }

        public GameDate getDate() {
            return date;
        }

        public Set<String> getVassals() {
            return vassals;
        }

        public Set<String> getAllies() {
            return allies;
        }

        public Set<String> getMarches() {
            return marches;
        }

        public Set<String> getMarriages() {
            return marriages;
        }

        public Set<String> getGuarantees() {
            return guarantees;
        }

        public Optional<String> getOverlord() {
            return overlord;
        }

        public Set<String> getJuniorPartners() {
            return juniorPartners;
        }

        public Optional<String> getSeniorPartner() {
            return seniorPartner;
        }

        public Set<String> getTributaryJuniors() {
            return tributaryJuniors;
        }

        public Optional<String> getTributarySenior() {
            return tributarySenior;
        }

        public Map<String, GameDate> getTruces() {
            return truces;
        }

        public Set<War> getWars() {
            return wars;
        }

        public UUID getSaveId() {
            return saveId;
        }
    }

    private UUID campaignId;
    private List<Entry> savegames;

    public Eu4Campaign(UUID campaignId, List<Entry> savegames) {
        this.campaignId = campaignId;
        this.savegames = savegames;
    }

    public static Eu4Campaign parse(Path p) throws IOException {
        String last = p.getName(p.getNameCount() - 1).toString();
        UUID id = UUID.fromString(last);

        File dir = p.toFile();
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };

        List<Entry> savegames = new ArrayList<>();
        for (File f : dir.listFiles()) {
            Entry entry = Entry.fromSavegame(Eu4IntermediateSavegame.fromFile(f.toPath()), id);
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
