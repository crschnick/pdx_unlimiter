package com.crschnick.pdxu.model.eu4;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Eu4SavegameInfo extends SavegameInfo<Eu4Tag> {

    private final List<Eu4Tag> vassals = new ArrayList<>();
    private final List<Eu4Tag> allies = new ArrayList<>();
    private final List<Eu4Tag> marches = new ArrayList<>();
    private final List<Eu4Tag> marriages = new ArrayList<>();
    private final List<Eu4Tag> guarantees = new ArrayList<>();
    private final Eu4Tag overlord = null;
    private final List<Eu4Tag> juniorPartners = new ArrayList<>();
    private final Eu4Tag seniorPartner = null;
    private final List<Eu4Tag> tributaryJuniors = new ArrayList<>();
    private final Eu4Tag tributarySenior = null;
    protected Eu4Tag tag;
    protected List<Eu4Tag> allTags;
    private boolean randomNewWorld;
    private boolean customNationInWorld;
    private boolean releasedVassal;
    private Ruler ruler;
    private Ruler heir;
    private int treasuryMoney;
    private int loanedMoney;
    private int manpower;
    private int maxManpower;
    private int totalDev;
    private int totalAutonomyDev;
    private int prestige;
    private int stability;
    private int adm;
    private int dip;
    private int mil;
    private List<War> wars = new ArrayList<>();
    private GameNamedVersion version;
    private boolean achievementOk;

    public static Eu4SavegameInfo fromSavegame(boolean melted, Node n) throws SavegameInfoException {
        try {
            GameDate date = GameDateType.EU4.fromString(n.getNodeForKey("date").getString());
            String tag = n.getNodeForKey("player").getString();
            Eu4SavegameInfo e = new Eu4SavegameInfo();

            Node ver = n.getNodeForKey("savegame_version");
            e.version = new GameNamedVersion(
                    ver.getNodeForKey("first").getInteger(),
                    ver.getNodeForKey("second").getInteger(),
                    ver.getNodeForKey("third").getInteger(),
                    ver.getNodeForKey("forth").getInteger(),
                    ver.getNodeForKey("name").getString());

            e.queryMods(n);

            e.dlcs = n.getNodeForKeyIfExistent("dlc_enabled").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            e.campaignHeuristic = UUID.nameUUIDFromBytes(n.getNodeForKey("countries")
                    .getNodeForKey("REB").getNodeForKey("decision_seed").getString().getBytes());

            e.allTags = new ArrayList<>();
            n.getNodeForKey("countries").forEach((k, v) -> {
                e.allTags.add(Eu4Tag.fromNode(k, v));
                if (v.hasKey("custom_nation_points")) {
                    e.customNationInWorld = true;
                }
            });
            e.observer = !n.getNodeForKeyIfExistent("not_observer").map(Node::getBoolean).orElse(false);
            e.randomNewWorld = n.getNodeForKeyIfExistent("is_random_new_world").map(Node::getBoolean).orElse(false);
            e.ironman = melted;
            e.binary = e.ironman;
            e.achievementOk = n.getNodeForKeyIfExistent("achievement_ok").map(Node::getBoolean).orElse(false);
                    e.releasedVassal = n.getNodeForKey("countries").getNodeForKey(tag)
                    .getNodeForKeyIfExistent("has_switched_nation").map(Node::getBoolean).orElse(false);
            e.date = date;

            e.tag = Eu4Tag.getTag(e.allTags, tag);

            if (e.observer) {
                return e;
            }

            AtomicInteger loans = new AtomicInteger();
            n.getNodeForKey("countries").getNodeForKey(tag).forEach((k, v) -> {
                if (k.equals("loan")) {
                    loans.addAndGet((int) v.getNodeForKey("amount").getDouble());
                }
            });
            e.loanedMoney = loans.get();

            e.treasuryMoney = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("treasury").getDouble();


            e.manpower = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("manpower").getDouble();
            e.maxManpower = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("max_manpower").getDouble();


            e.stability = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("stability").getDouble();


            e.adm = n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("powers").getNodeArray().get(0).getInteger();
            e.dip = n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("powers").getNodeArray().get(1).getInteger();
            e.mil = n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("powers").getNodeArray().get(2).getInteger();


            e.prestige = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("prestige").getDouble();
            e.totalDev = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("raw_development").getDouble();
            e.totalAutonomyDev = (int) n.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("development").getDouble();


            e.wars = War.fromActiveWarsNode(e.allTags, tag, n);
            e.ruler = Ruler.fromCountryNode(n.getNodeForKey("countries").getNodeForKey(tag),"monarch").orElse(
                    new Ruler("MISSING", "MISSING RULER", -1, -1, -1));
            e.heir = Ruler.fromCountryNode(n.getNodeForKey("countries").getNodeForKey(tag), "heir").orElse(null);
            for (Node dep : n.getNodeForKey("diplomacy").getNodesForKey("dependency")) {
                String first = dep.getNodeForKey("first").getString();
                String second = dep.getNodeForKey("second").getString();
                String type = dep.getNodeForKey("subject_type").getString();
                if (first.equals(tag)) {
                    if (type.equals("vassal")) {
                        e.vassals.add(Eu4Tag.getTag(e.allTags, second));
                    }
                    if (type.equals("daimyo_vassal")) {
                        e.vassals.add(Eu4Tag.getTag(e.allTags, second));
                    }
                    if (type.equals("personal_union")) {
                        e.juniorPartners.add(Eu4Tag.getTag(e.allTags, second));
                    }
                    if (type.equals("tributary_state")) {
                        e.tributaryJuniors.add(Eu4Tag.getTag(e.allTags, second));
                    }
                }

                if (second.equals(tag)) {
                    if (type.equals("vassal")) {
                        e.vassals.add(Eu4Tag.getTag(e.allTags, first));
                    }
                    if (type.equals("daimyo_vassal")) {
                        e.vassals.add(Eu4Tag.getTag(e.allTags, first));
                    }
                    if (type.equals("personal_union")) {
                        e.juniorPartners.add(Eu4Tag.getTag(e.allTags, first));
                    }
                    if (type.equals("tributary_state")) {
                        e.tributaryJuniors.add(Eu4Tag.getTag(e.allTags, first));
                    }
                }

            }

            for (Node alli : n.getNodeForKey("diplomacy").getNodesForKey("alliance")) {
                String first = alli.getNodeForKey("first").getString();
                String second = alli.getNodeForKey("second").getString();
                if (first.equals(tag)) {
                    e.allies.add(Eu4Tag.getTag(e.allTags, second));
                }
                if (second.equals(tag)) {
                    e.allies.add(Eu4Tag.getTag(e.allTags, first));
                }
            }

            for (Node rm : n.getNodeForKey("diplomacy").getNodesForKey("royal_marriage")) {
                String first = rm.getNodeForKey("first").getString();
                String second = rm.getNodeForKey("second").getString();
                if (first.equals(tag)) {
                    e.marriages.add(Eu4Tag.getTag(e.allTags, second));
                }
                if (second.equals(tag)) {
                    e.marriages.add(Eu4Tag.getTag(e.allTags, first));
                }
            }

            for (Node guar : n.getNodeForKey("diplomacy").getNodesForKey("guarantee")) {
                String first = guar.getNodeForKey("first").getString();
                String second = guar.getNodeForKey("second").getString();
                if (first.equals(tag)) {
                    e.guarantees.add(Eu4Tag.getTag(e.allTags, second));
                }
            }

            return e;
        } catch (Throwable ex) {
            throw new SavegameInfoException("Error while creating savegame info", ex);
        }
    }

    private void queryMods(Node n) {
        // Mod data has changed in 1.31
        if (version.compareTo(new GameVersion(1, 31, 0, 0)) >= 0) {
            var list = new ArrayList<String>();
            n.getNodeForKeyIfExistent("mods_enabled_names").ifPresent(me -> me.forEach((k, v) -> {
                list.add(v.getNodeForKey("filename").getString());
            }, true));
            mods = list;
        } else {
            mods = n.getNodeForKeyIfExistent("mod_enabled").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());
        }
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
        return Optional.ofNullable(heir);
    }

    public List<Eu4Tag> getVassals() {
        return vassals;
    }

    public List<Eu4Tag> getAllies() {
        return allies;
    }

    public List<Eu4Tag> getMarches() {
        return marches;
    }

    public List<Eu4Tag> getMarriages() {
        return marriages;
    }

    public List<Eu4Tag> getGuarantees() {
        return guarantees;
    }

    public List<Eu4Tag> getJuniorPartners() {
        return juniorPartners;
    }

    public List<Eu4Tag> getTributaryJuniors() {
        return tributaryJuniors;
    }

    public List<War> getWars() {
        return wars;
    }

    public Eu4Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    public List<Eu4Tag> getAllTags() {
        return allTags;
    }

    public int getTreasuryMoney() {
        return treasuryMoney;
    }

    public int getLoanedMoney() {
        return loanedMoney;
    }

    public int getManpower() {
        return manpower;
    }

    public int getMaxManpower() {
        return maxManpower;
    }

    public int getStability() {
        return stability;
    }

    public int getTotalDev() {
        return totalDev;
    }

    public int getPrestige() {
        return prestige;
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

    public static class Ruler {

        private String name;
        private String fullName;
        private int adm;
        private int dip;
        private int mil;

        public Ruler() {
        }

        public Ruler(String name, String fullName, int adm, int dip, int mil) {
            this.name = name;
            this.fullName = fullName;
            this.adm = adm;
            this.dip = dip;
            this.mil = mil;
        }

        public static Optional<Ruler> fromCountryNode(Node n, String t) {
            if (!n.hasKey(t)) {
                return Optional.empty();
            }

            int personId = n.getNodeForKey(t).getNodeForKey("id").getInteger();
            AtomicReference<Optional<Ruler>> current = new AtomicReference<>(Optional.empty());
            n.getNodeForKey("history").forEach((k, v) -> {
                for (String type : new String[] {"monarch_heir", "monarch", "queen", "heir"}) {
                    if (GameDateType.EU4.isDate(k) && v.hasKey(type)) {
                        // Sometimes there are multiple monarchs in one event Node ... wtf?
                        for (Node r : v.getNodesForKey(type)) {
                            if (!r.hasKey("id")) {
                                continue;
                            }

                            int rId = r.getNodeForKey("id").getNodeForKey("id").getInteger();
                            if (rId == personId) {
                                String name = r.getNodeForKey("name").getString();
                                String fullName = name;
                                if (r.hasKey("dynasty")) {
                                    fullName = name + " " + r.getNodeForKey("dynasty").getString();
                                }
                                current.set(Optional.of(new Ruler(
                                        name,
                                        fullName,
                                        r.getNodeForKey("ADM").getInteger(),
                                        r.getNodeForKey("DIP").getInteger(),
                                        r.getNodeForKey("MIL").getInteger())));
                                return;
                            }
                        }
                    }
                }
            });
            return current.get();
        }

        public String getName() {
            return name;
        }

        public String getFullName() {
            return fullName;
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
        private List<Eu4Tag> allies;
        private List<Eu4Tag> enemies;

        public War() {
        }

        public War(String title, boolean attacker, List<Eu4Tag> allies, List<Eu4Tag> enemies) {
            this.title = title;
            this.attacker = attacker;
            this.allies = allies;
            this.enemies = enemies;
        }

        public static List<War> fromActiveWarsNode(List<Eu4Tag> tags, String tag, Node n) {
            List<War> wars = new ArrayList<>();
            for (Node war : n.getNodesForKey("active_war")) {
                String title = war.getNodeForKeyIfExistent("name").map(Node::getString).orElse("MISSING NAME");
                boolean isAttacker = false;
                List<Eu4Tag> attackers = new ArrayList<>();
                if (war.hasKey("attackers")) {
                    for (Node atk : war.getNodeForKey("attackers").getNodeArray()) {
                        String attacker = atk.getString();
                        if (attacker.equals(tag)) {
                            isAttacker = true;
                        } else {
                            attackers.add(Eu4Tag.getTag(tags, attacker));
                        }
                    }
                }

                boolean isDefender = false;
                List<Eu4Tag> defenders = new ArrayList<>();
                if (war.hasKey("defenders")) {
                    for (Node def : war.getNodeForKey("defenders").getNodeArray()) {
                        String defender = def.getString();
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

        public List<Eu4Tag> getEnemies() {
            return enemies;
        }
    }

    public boolean isAchievementOk() {
        return achievementOk;
    }

    public int getTotalAutonomyDev() {
        return totalAutonomyDev;
    }
}
