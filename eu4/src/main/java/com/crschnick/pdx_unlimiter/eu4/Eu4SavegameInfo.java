package com.crschnick.pdx_unlimiter.eu4;

import com.crschnick.pdx_unlimiter.eu4.parser.*;

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
        private Set<GameTag> allies;
        private Set<GameTag> enemies;

        public War(String title, boolean attacker, Set<GameTag> allies, Set<GameTag> enemies) {
            this.title = title;
            this.attacker = attacker;
            this.allies = allies;
            this.enemies = enemies;
        }

        public String getTitle() {
            return title;
        }

        public Set<GameTag> getEnemies() {
            return enemies;
        }

        public static Set<War> fromActiveWarsNode(Set<GameTag> tags, String tag, Node n) {
            Set<War> wars = new HashSet<>();
            for (Node war : Node.getNodeArray(n)) {
                String title = Node.getString(Node.getNodeForKey(war, "name"));
                boolean isAttacker = false;
                Set<GameTag> attackers = new HashSet<>();
                if (Node.hasKey(war, "attackers")) {
                    for (Node atk : Node.getNodeArray(Node.getNodeForKey(war, "attackers"))) {
                        String attacker = Node.getString(atk);
                        if (attacker.equals(tag)) {
                            isAttacker = true;
                        } else {
                            attackers.add(GameTag.getTag(tags, attacker));
                        }
                    }
                }

                boolean isDefender = false;
                Set<GameTag> defenders = new HashSet<>();
                if (Node.hasKey(war, "defenders")) {
                    for (Node def : Node.getNodeArray(Node.getNodeForKey(war, "defenders"))) {
                        String defender = Node.getString(def);
                        if (defender.equals(tag)) {
                            isDefender = true;
                        } else {
                            defenders.add(GameTag.getTag(tags, defender));
                        }
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

    private boolean ironman;
    private boolean randomNewWorld;
    private boolean customNationInWorld;
    private boolean releasedVassal;

    private Set<GameTag> allTags = new HashSet<>();
    private GameTag currentTag;
    private GameDate date;
    private GameVersion version;
    private Ruler ruler;
    private Optional<Ruler> heir;
    private Set<GameTag> vassals = new HashSet<>();
    private Set<GameTag> allies = new HashSet<>();
    private Set<GameTag> marches = new HashSet<>();
    private Set<GameTag> marriages = new HashSet<>();
    private Set<GameTag> guarantees = new HashSet<>();
    private Optional<GameTag> overlord = Optional.empty();
    private Set<GameTag> juniorPartners = new HashSet<>();
    private Optional<GameTag> seniorPartner = Optional.empty();
    private Set<GameTag> tributaryJuniors = new HashSet<>();
    private Optional<GameTag> tributarySenior = Optional.empty();
    private Map<GameTag, GameDate> truces = new HashMap<>();
    private Set<War> wars = new HashSet<>();


    public static Eu4SavegameInfo fromSavegame(Eu4IntermediateSavegame save) throws SavegameParseException {
        try {
            GameDate date = GameDate.fromNode(Node.getNodeForKey(save.getNodes().get("meta"), "date"));
            String tag = Node.getString(Node.getNodeForKey(save.getNodes().get("meta"), "player"));
            Eu4SavegameInfo e = new Eu4SavegameInfo();

            for (Node n : Node.getNodeArray(save.getNodes().get("countries"))) {
                e.allTags.add(GameTag.fromNode(n));
                if (Node.hasKey(Node.getKeyValueNode(n).getNode(), "custom_nation_points")) {
                    e.customNationInWorld = true;
                }
            }
            e.randomNewWorld = Node.getBoolean(Node.getNodeForKey(save.getNodes().get("meta"), "is_random_new_world"));
            e.ironman = Node.getBoolean(Node.getNodeForKey(save.getNodes().get("meta"), "ironman"));
            e.releasedVassal = Node.hasKey(Node.getNodeForKey(save.getNodes().get("countries"), tag), "has_switched_nation");
            e.date = date;
            e.currentTag = GameTag.getTag(e.allTags, tag);
            e.wars = War.fromActiveWarsNode(e.allTags, tag, save.getNodes().get("active_wars"));
            e.ruler = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries_history"), tag), "monarch").get();
            e.heir = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries_history"), tag), "heir");
            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "dependencies"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal")) {
                        e.vassals.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal")) {
                        e.vassals.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union")) {
                        e.juniorPartners.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state")) {
                        e.tributaryJuniors.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                }

                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal")) {
                        e.overlord = Optional.of(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal")) {
                        e.overlord = Optional.of(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union")) {
                        e.seniorPartner = Optional.of(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state")) {
                        e.tributarySenior = Optional.of(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                }

            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "alliances"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.allies.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                }
                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    e.allies.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                }
            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "royal_marriages"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.marriages.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                }
                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    e.marriages.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                }
            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "guarantees"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.guarantees.add(GameTag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                }
            }


            Node v = Node.getNodeForKey(save.getNodes().get("meta"), "savegame_version");
            e.version = new GameVersion(Node.getInteger(Node.getNodeForKey(v, "first")),
                    Node.getInteger(Node.getNodeForKey(v, "second")),
                    Node.getInteger(Node.getNodeForKey(v, "third")),
                    Node.getInteger(Node.getNodeForKey(v, "forth")));

            return e;
        } catch (NodeFormatException ex) {
            throw new SavegameParseException("Error while creating savegame info", ex);
        }
    }

    public boolean isIronman() {
        return ironman;
    }

    public boolean isRandomNewWorld() {
        return randomNewWorld;
    }

    public boolean isCustomNationInWorld() {
        return customNationInWorld;
    }

    public boolean isReleasedVassal() {
        return releasedVassal;
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

    public GameDate getDate() {
        return date;
    }

    public Set<GameTag> getAllTags() {
        return allTags;
    }

    public GameTag getCurrentTag() {
        return currentTag;
    }

    public Set<GameTag> getVassals() {
        return vassals;
    }

    public Set<GameTag> getAllies() {
        return allies;
    }

    public Set<GameTag> getMarches() {
        return marches;
    }

    public Set<GameTag> getMarriages() {
        return marriages;
    }

    public Set<GameTag> getGuarantees() {
        return guarantees;
    }

    public Optional<GameTag> getOverlord() {
        return overlord;
    }

    public Set<GameTag> getJuniorPartners() {
        return juniorPartners;
    }

    public Optional<GameTag> getSeniorPartner() {
        return seniorPartner;
    }

    public Set<GameTag> getTributaryJuniors() {
        return tributaryJuniors;
    }

    public Optional<GameTag> getTributarySenior() {
        return tributarySenior;
    }

    public Map<GameTag, GameDate> getTruces() {
        return truces;
    }

    public Set<War> getWars() {
        return wars;
    }
}
