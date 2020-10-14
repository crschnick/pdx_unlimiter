package com.crschnick.pdx_unlimiter.eu4.parser;

import java.util.*;

public class Eu4SavegameInfo{

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
            for (Node e : Node.getNodeArray(Node.getNodeForKey(n,"events"))) {
                if (Node.hasKey(e, type)) {
                    // Sometimes there are multiple monarchs in one event Node ... wtf?
                    Node r = Node.getNodesForKey(e, type).get(0);
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
        private boolean attacker;
        private Set<String> allies;
        private Set<String> enemies;

        public War(String title, boolean attacker, Set<String> allies, Set<String> enemies) {
            this.title = title;
            this.attacker = attacker;
            this.allies = allies;
            this.enemies = enemies;
        }

        public String getTitle() {
            return title;
        }

        public Set<String> getEnemies() {
            return enemies;
        }

        public static Set<War> fromActiveWarsNode(String tag, Node n) {
            Set<War> wars = new HashSet<>();
            for (Node war : Node.getNodeArray(n)) {
                String title = Node.getString(Node.getNodeForKey(war, "name"));
                boolean isAttacker = false;
                Set<String> attackers = new HashSet<>();
                for (Node atk : Node.getNodeArray(Node.getNodeForKey(war, "attackers"))) {
                    String attacker = Node.getString(atk);
                    if (attacker.equals(tag)) {
                        isAttacker = true;
                    } else {
                        attackers.add(attacker);
                    }
                }

                boolean isDefender = false;
                Set<String> defenders = new HashSet<>();
                for (Node def : Node.getNodeArray(Node.getNodeForKey(war, "defenders"))) {
                    String defender = Node.getString(def);
                    if (defender.equals(tag)) {
                        isDefender = true;
                    } else {
                        defenders.add(defender);
                    }
                }
                if (isAttacker) {
                    wars.add(new War(title, true, attackers, defenders));
                } else if (isDefender) {

                    wars.add(new War(title, false, defenders, attackers));
                }
            }
            return wars;
        }
    }

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


    public static Eu4SavegameInfo fromSavegame(Eu4IntermediateSavegame save) {
        GameDate date = GameDate.fromNode(Node.getNodeForKey(save.getNodes().get("meta"), "date"));
        String tag = Node.getString(Node.getNodeForKey(save.getNodes().get("meta"), "player"));
        Eu4SavegameInfo e = new Eu4SavegameInfo();
        e.date = date;
        e.currentTag = tag;
        e.wars = War.fromActiveWarsNode(tag, save.getNodes().get("active_wars"));
        e.ruler = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries_history"), tag), "monarch").get();
        e.heir = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries_history"), tag), "heir");
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
}
