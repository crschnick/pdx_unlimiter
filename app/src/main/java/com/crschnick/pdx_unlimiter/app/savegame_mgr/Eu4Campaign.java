package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.sql.Timestamp;
import java.util.*;

public class Eu4Campaign implements Comparable<Eu4Campaign> {

    public static class Entry implements Comparable<Entry> {

        @Override
        public int compareTo(Entry o) {
            return this.date.compareTo(o.date);
        }

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

        private StringProperty name;
        private String currentTag;
        private GameDate date;
        private GameVersion version;
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
        private Set<War> wars = new HashSet<>();
        ;
        private UUID saveId;

        public static Entry fromSavegame(Eu4IntermediateSavegame save, UUID saveId) {
            GameDate date = GameDate.fromNode(Node.getNodeForKey(save.getNodes().get("meta"), "date"));
            String tag = Node.getString(Node.getNodeForKey(save.getNodes().get("meta"), "player"));
            Entry e = new Entry();
            e.name = new SimpleStringProperty(date.toDisplayString());
            e.saveId = saveId;
            e.date = date;
            e.currentTag = tag;
            e.ruler = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries"), tag), "monarch").get();
            e.heir = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries"), tag), "heir");
            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "dependencies"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal")) {
                        e.vassals.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal")) {
                        e.vassals.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union")) {
                        e.juniorPartners.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state")) {
                        e.tributaryJuniors.add(Node.getString(Node.getNodeForKey(n, "second")));
                    }
                }

                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal")) {
                        e.overlord = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal")) {
                        e.overlord = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union")) {
                        e.seniorPartner = Optional.of(Node.getString(Node.getNodeForKey(n, "first")));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state")) {
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
            e.version = new GameVersion(Node.getInteger(Node.getNodeForKey(v, "first")),
                    Node.getInteger(Node.getNodeForKey(v, "second")),
                    Node.getInteger(Node.getNodeForKey(v, "third")),
                    Node.getInteger(Node.getNodeForKey(v, "forth")));

            return e;
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public Ruler getRuler() {
            return ruler;
        }

        public Optional<Ruler> getHeir() {
            return heir;
        }

        public GameVersion getVersion() {
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

    private ObjectProperty<Timestamp> lastPlayed;
    private StringProperty tag;
    private StringProperty name;
    private ObjectProperty<GameDate> date;
    private UUID campaignId;
    private ObservableSet<Entry> savegames = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new TreeSet<>()));

    public Eu4Campaign(ObjectProperty<Timestamp> lastPlayed, StringProperty tag, StringProperty name, ObjectProperty<GameDate> date, UUID campaignId) {
        this.lastPlayed = lastPlayed;
        this.tag = tag;
        this.name = name;
        this.date = date;
        this.campaignId = campaignId;
    }

    @Override
    public int compareTo(Eu4Campaign o) {
        return this.lastPlayed.get().compareTo(o.lastPlayed.get());
    }

    public void add(Entry e) {
        this.savegames.add(e);
    }

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public ObservableSet<Entry> getSavegames() {
        return savegames;
    }

    public Timestamp getLastPlayed() {
        return lastPlayed.get();
    }

    public ObjectProperty<Timestamp> lastPlayedProperty() {
        return lastPlayed;
    }
}
