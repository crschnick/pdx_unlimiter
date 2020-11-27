package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.data.Eu4Date;
import com.crschnick.pdx_unlimiter.eu4.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.NodeFormatException;

import java.util.*;
import java.util.stream.Collectors;

public class Eu4SavegameInfo extends SavegameInfo {

    private UUID campaignUuid;
    private boolean ironman;
    private boolean randomNewWorld;
    private boolean customNationInWorld;
    private boolean releasedVassal;
    private boolean observer;
    private Set<Eu4Tag> allTags = new HashSet<>();
    private Eu4Tag currentTag;
    private Eu4Date date;
    private Ruler ruler;
    private Optional<Ruler> heir;
    private Set<Eu4Tag> vassals = new HashSet<>();
    private Set<Eu4Tag> allies = new HashSet<>();
    private Set<Eu4Tag> marches = new HashSet<>();
    private Set<Eu4Tag> marriages = new HashSet<>();
    private Set<Eu4Tag> guarantees = new HashSet<>();
    private Optional<Eu4Tag> overlord = Optional.empty();
    private Set<Eu4Tag> juniorPartners = new HashSet<>();
    private Optional<Eu4Tag> seniorPartner = Optional.empty();
    private Set<Eu4Tag> tributaryJuniors = new HashSet<>();
    private Optional<Eu4Tag> tributarySenior = Optional.empty();
    private Map<Eu4Tag, Eu4Date> truces = new HashMap<>();
    private Set<War> wars = new HashSet<>();

    public static Eu4SavegameInfo fromSavegame(Eu4Savegame save) throws SavegameParseException {
        try {
            Eu4Date date = Eu4Date.fromNode(Node.getNodeForKey(save.getNodes().get("meta"), "date"));
            String tag = Node.getString(Node.getNodeForKey(save.getNodes().get("meta"), "player"));
            Eu4SavegameInfo e = new Eu4SavegameInfo();

            e.mods = Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("gamestate"), "mod_enabled"))
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            e.dlcs = Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("meta"), "dlc_enabled"))
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            e.campaignUuid = UUID.fromString(Node.getString(Node.getNodeForKey(save.getNodes().get("meta"), "campaign_id")));

            for (Node n : Node.getNodeArray(save.getNodes().get("countries"))) {
                e.allTags.add(Eu4Tag.fromNode(n));
                if (Node.hasKey(Node.getKeyValueNode(n).getNode(), "custom_nation_points")) {
                    e.customNationInWorld = true;
                }
            }
            e.observer = !Node.getBoolean(Node.getNodeForKey(save.getNodes().get("meta"), "not_observer"));
            e.randomNewWorld = Node.getBoolean(Node.getNodeForKey(save.getNodes().get("meta"), "is_random_new_world"));
            e.ironman = Node.getBoolean(Node.getNodeForKey(save.getNodes().get("meta"), "ironman"));
            e.releasedVassal = Node.hasKey(Node.getNodeForKey(save.getNodes().get("countries"), tag), "has_switched_nation");
            e.date = date;

            Node v = Node.getNodeForKey(save.getNodes().get("meta"), "savegame_version");
            e.version = new GameVersion(
                    Node.getInteger(Node.getNodeForKey(v, "first")),
                    Node.getInteger(Node.getNodeForKey(v, "second")),
                    Node.getInteger(Node.getNodeForKey(v, "third")),
                    Node.getInteger(Node.getNodeForKey(v, "forth")),
                    Node.getString(Node.getNodeForKey(v, "name")));

            e.currentTag = Eu4Tag.getTag(e.allTags, tag);

            if (e.observer) {
                return e;
            }

            e.wars = War.fromActiveWarsNode(e.allTags, tag, save.getNodes().get("active_wars"));
            e.ruler = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries_history"), tag), "monarch").get();
            e.heir = Ruler.fromCountryNode(Node.getNodeForKey(save.getNodes().get("countries_history"), tag), "heir");
            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "dependencies"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal")) {
                        e.vassals.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal")) {
                        e.vassals.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union")) {
                        e.juniorPartners.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state")) {
                        e.tributaryJuniors.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                    }
                }

                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("vassal")) {
                        e.overlord = Optional.of(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("daimyo_vassal")) {
                        e.overlord = Optional.of(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("personal_union")) {
                        e.seniorPartner = Optional.of(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                    if (Node.getString(Node.getNodeForKey(n, "subject_type")).equals("tributary_state")) {
                        e.tributarySenior = Optional.of(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                    }
                }

            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "alliances"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.allies.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                }
                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    e.allies.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                }
            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "royal_marriages"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.marriages.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                }
                if (Node.getString(Node.getNodeForKey(n, "second")).equals(tag)) {
                    e.marriages.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "first"))));
                }
            }

            for (Node n : Node.getNodeArray(Node.getNodeForKey(save.getNodes().get("diplomacy"), "guarantees"))) {
                if (Node.getString(Node.getNodeForKey(n, "first")).equals(tag)) {
                    e.guarantees.add(Eu4Tag.getTag(e.allTags, Node.getString(Node.getNodeForKey(n, "second"))));
                }
            }

            return e;
        } catch (NodeFormatException ex) {
            throw new SavegameParseException("Error while creating savegame info", ex);
        }
    }

    public boolean isObserver() {
        return observer;
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

    public Eu4Date getDate() {
        return date;
    }

    public Set<Eu4Tag> getAllTags() {
        return allTags;
    }

    public Eu4Tag getCurrentTag() {
        return currentTag;
    }

    public Set<Eu4Tag> getVassals() {
        return vassals;
    }

    public Set<Eu4Tag> getAllies() {
        return allies;
    }

    public Set<Eu4Tag> getMarches() {
        return marches;
    }

    public Set<Eu4Tag> getMarriages() {
        return marriages;
    }

    public Set<Eu4Tag> getGuarantees() {
        return guarantees;
    }

    public Optional<Eu4Tag> getOverlord() {
        return overlord;
    }

    public Set<Eu4Tag> getJuniorPartners() {
        return juniorPartners;
    }

    public Optional<Eu4Tag> getSeniorPartner() {
        return seniorPartner;
    }

    public Set<Eu4Tag> getTributaryJuniors() {
        return tributaryJuniors;
    }

    public Optional<Eu4Tag> getTributarySenior() {
        return tributarySenior;
    }

    public Map<Eu4Tag, Eu4Date> getTruces() {
        return truces;
    }

    public Set<War> getWars() {
        return wars;
    }

    public UUID getCampaignUuid() {
        return campaignUuid;
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

        public static Optional<Ruler> fromCountryNode(Node n, String type) {
            Optional<Ruler> current = Optional.empty();
            for (Node e : Node.getNodeArray(Node.getNodeForKey(n, "events"))) {
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
    }

    public static class War {

        private String title;
        private boolean attacker;
        private Set<Eu4Tag> allies;
        private Set<Eu4Tag> enemies;

        public War(String title, boolean attacker, Set<Eu4Tag> allies, Set<Eu4Tag> enemies) {
            this.title = title;
            this.attacker = attacker;
            this.allies = allies;
            this.enemies = enemies;
        }

        public static Set<War> fromActiveWarsNode(Set<Eu4Tag> tags, String tag, Node n) {
            Set<War> wars = new HashSet<>();
            for (Node war : Node.getNodeArray(n)) {
                String title = Node.getString(Node.getNodeForKey(war, "name"));
                boolean isAttacker = false;
                Set<Eu4Tag> attackers = new HashSet<>();
                if (Node.hasKey(war, "attackers")) {
                    for (Node atk : Node.getNodeArray(Node.getNodeForKey(war, "attackers"))) {
                        String attacker = Node.getString(atk);
                        if (attacker.equals(tag)) {
                            isAttacker = true;
                        } else {
                            attackers.add(Eu4Tag.getTag(tags, attacker));
                        }
                    }
                }

                boolean isDefender = false;
                Set<Eu4Tag> defenders = new HashSet<>();
                if (Node.hasKey(war, "defenders")) {
                    for (Node def : Node.getNodeArray(Node.getNodeForKey(war, "defenders"))) {
                        String defender = Node.getString(def);
                        if (defender.equals(tag)) {
                            isDefender = true;
                        } else {
                            defenders.add(Eu4Tag.getTag(tags, defender));
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

        public String getTitle() {
            return title;
        }

        public Set<Eu4Tag> getEnemies() {
            return enemies;
        }
    }
}
