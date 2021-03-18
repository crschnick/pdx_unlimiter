package com.crschnick.pdx_unlimiter.core.info.eu4;

import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.node.NodeFormatException;
import com.crschnick.pdx_unlimiter.core.parser.ParseException;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Eu4SavegameInfo extends SavegameInfo<Eu4Tag> {

    private final Set<Eu4Tag> vassals = new HashSet<>();
    private final Set<Eu4Tag> allies = new HashSet<>();
    private final Set<Eu4Tag> marches = new HashSet<>();
    private final Set<Eu4Tag> marriages = new HashSet<>();
    private final Set<Eu4Tag> guarantees = new HashSet<>();
    private final Eu4Tag overlord = null;
    private final Set<Eu4Tag> juniorPartners = new HashSet<>();
    private final Eu4Tag seniorPartner = null;
    private final Set<Eu4Tag> tributaryJuniors = new HashSet<>();
    private final Eu4Tag tributarySenior = null;
    protected Eu4Tag tag;
    protected Set<Eu4Tag> allTags;
    private boolean randomNewWorld;
    private boolean customNationInWorld;
    private boolean releasedVassal;
    private boolean observer;
    private Ruler ruler;
    private Ruler heir;
    private Set<War> wars = new HashSet<>();

    public static Eu4SavegameInfo fromSavegame(boolean melted, Node n) throws ParseException {
        try {
            GameDate date = GameDateType.EU4.fromString(n.getNodeForKey("date").getString());
            String tag = n.getNodeForKey("player").getString();
            Eu4SavegameInfo e = new Eu4SavegameInfo();

            e.mods = n.getNodeForKeyIfExistent("mod_enabled").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            e.dlcs = n.getNodeForKeyIfExistent("dlc_enabled").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            e.campaignHeuristic = UUID.nameUUIDFromBytes(n.getNodeForKey("countries")
                    .getNodeForKey("REB").getNodeForKey("decision_seed").getString().getBytes());

            e.allTags = new HashSet<>();
            n.getNodeForKey("countries").forEach((k, v) -> {
                e.allTags.add(Eu4Tag.fromNode(k, v));
                if (v.hasKey("custom_nation_points")) {
                    e.customNationInWorld = true;
                }
            });
            e.observer = !n.getNodeForKey("not_observer").getBoolean();
            e.randomNewWorld = n.getNodeForKeyIfExistent("is_random_new_world").map(Node::getBoolean).orElse(false);
            e.ironman = melted;
            e.binary = e.ironman;
            e.releasedVassal = n.getNodeForKey("countries").getNodeForKey(tag)
                    .getNodeForKeyIfExistent("has_switched_nation").map(Node::getBoolean).orElse(false);
            e.date = date;

            Node v = n.getNodeForKey("savegame_version");
            e.version = new GameVersion(
                    v.getNodeForKey("first").getInteger(),
                    v.getNodeForKey("second").getInteger(),
                    v.getNodeForKey("third").getInteger(),
                    v.getNodeForKey("forth").getInteger(),
                    v.getNodeForKey("name").getString());

            e.tag = Eu4Tag.getTag(e.allTags, tag);

            if (e.observer) {
                return e;
            }

            e.wars = War.fromActiveWarsNode(e.allTags, tag, n);
            e.ruler = Ruler.fromCountryNode(e.date, n.getNodeForKey("countries").getNodeForKey(tag),
                    "monarch_heir", "monarch", "queen").get();
            e.heir = Ruler.fromCountryNode(e.date,
                    n.getNodeForKey("countries").getNodeForKey(tag), "heir").orElse(null);
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

            for (Node rm : n.getNodeForKey("diplomacy").getNodesForKey("royal_marriages")) {
                String first = rm.getNodeForKey("first").getString();
                String second = rm.getNodeForKey("second").getString();
                if (first.equals(tag)) {
                    e.marriages.add(Eu4Tag.getTag(e.allTags, second));
                }
                if (second.equals(tag)) {
                    e.marriages.add(Eu4Tag.getTag(e.allTags, first));
                }
            }

            for (Node guar : n.getNodeForKey("diplomacy").getNodesForKey("guarantees")) {
                String first = guar.getNodeForKey("first").getString();
                String second = guar.getNodeForKey("second").getString();
                if (first.equals(tag)) {
                    e.guarantees.add(Eu4Tag.getTag(e.allTags, second));
                }
            }

            return e;
        } catch (NodeFormatException ex) {
            throw new ParseException("Error while creating savegame info", ex);
        }
    }

    public boolean isObserver() {
        return observer;
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
        return Optional.ofNullable(overlord);
    }

    public Set<Eu4Tag> getJuniorPartners() {
        return juniorPartners;
    }

    public Optional<Eu4Tag> getSeniorPartner() {
        return Optional.ofNullable(seniorPartner);
    }

    public Set<Eu4Tag> getTributaryJuniors() {
        return tributaryJuniors;
    }

    public Optional<Eu4Tag> getTributarySenior() {
        return Optional.ofNullable(tributarySenior);
    }

    public Map<Eu4Tag, GameDate> getTruces() {
        return Map.of();
    }

    public Set<War> getWars() {
        return wars;
    }

    public Eu4Tag getTag() {
        return tag;
    }

    public Set<Eu4Tag> getAllTags() {
        return allTags;
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

        public static Optional<Ruler> fromCountryNode(GameDate date, Node n, String... types) {
            AtomicReference<Optional<Ruler>> current = new AtomicReference<>(Optional.empty());
            n.getNodeForKey("history").forEach((k, v) -> {
                for (String type : types) {
                    if (GameDateType.EU4.isDate(k) && v.hasKey(type)) {
                        // Sometimes there are multiple monarchs in one event Node ... wtf?
                        Node r = v.getNodesForKey(type).get(0);

                        // Exclude queen consorts
                        if (r.hasKey("consort")) {
                            continue;
                        }

                        // Exclude dead rulers
                        if (r.hasKey("death_date")) {
                            boolean dead = GameDateType.EU4.fromString(r.getNodeForKey("death_date").getString())
                                    .compareTo(date) <= 0;
                            if (dead) {
                                current.set(Optional.empty());
                                continue;
                            }
                        }

                        // If we have a new heir, but the heir has already succeeded, set the current heir to null
                        boolean succeeded = type.equals("heir") && r.hasKey("succeeded");
                        if (succeeded) {
                            current.set(Optional.empty());
                            continue;
                        }

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
        private Set<Eu4Tag> allies;
        private Set<Eu4Tag> enemies;

        public War() {
        }

        public War(String title, boolean attacker, Set<Eu4Tag> allies, Set<Eu4Tag> enemies) {
            this.title = title;
            this.attacker = attacker;
            this.allies = allies;
            this.enemies = enemies;
        }

        public static Set<War> fromActiveWarsNode(Set<Eu4Tag> tags, String tag, Node n) {
            Set<War> wars = new HashSet<>();
            for (Node war : n.getNodesForKey("active_war")) {
                String title = war.getNodeForKey("name").getString();
                boolean isAttacker = false;
                Set<Eu4Tag> attackers = new HashSet<>();
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
                Set<Eu4Tag> defenders = new HashSet<>();
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

        public Set<Eu4Tag> getEnemies() {
            return enemies;
        }
    }
}
