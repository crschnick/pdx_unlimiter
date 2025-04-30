package com.crschnick.pdxu.model.ck3;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.coa.CoatOfArms;

import java.util.*;
import java.util.stream.Collectors;

public class Ck3Tag {

    private long id;
    private Ck3Person ruler;
    private List<Ck3Title> titles;
    private List<Ck3Title> claims;
    private String governmentName;
    private CoatOfArms coatOfArms;
    private String name;
    private int balance;
    private int strength;
    private int gold;
    private int income;
    private int piety;
    private int prestige;

    public Ck3Tag() {
    }

    public Ck3Tag(long id, Ck3Person ruler, List<Ck3Title> titles, List<Ck3Title> claims, String governmentName, CoatOfArms coatOfArms, String name, int balance, int strength, int gold, int income, int piety, int prestige) {
        this.id = id;
        this.ruler = ruler;
        this.titles = titles;
        this.claims = claims;
        this.governmentName = governmentName;
        this.coatOfArms = coatOfArms;
        this.name = name;
        this.balance = balance;
        this.strength = strength;
        this.gold = gold;
        this.income = income;
        this.piety = piety;
        this.prestige = prestige;
    }

    public static Optional<Ck3Tag> getTag(List<Ck3Tag> tags, long id) {
        return tags.stream().filter(t -> t.id == id).findFirst();
    }

    public static Optional<Ck3Tag> getPlayerTag(Node n, List<Ck3Tag> allTags) {
        // Observer check
        if (!n.hasKey("currently_played_characters")) {
            return Optional.empty();
        }

        // Multiplayer check
        if (n.getNodeForKey("currently_played_characters").getNodeArray().size() > 1) {
            return Optional.empty();
        }

        long id = n.getNodeForKey("currently_played_characters").getNodeArray().get(0).getLong();
        if (!n.getNodeForKey("living").hasKey(String.valueOf(id))) {
            return Optional.empty();
        }

        var personNode = n.getNodeForKey("living").getNodeForKey(String.valueOf(id));
        if (!personNode.hasKey("landed_data")) {
            return Optional.empty();
        }

        var house = new Ck3House(
                Ck3Strings.cleanCk3FormatData(n.getNodeForKey("meta_data").getNodeForKey("meta_house_name").getString()),
                CoatOfArms.fromNode(n.getNodeForKey("meta_data").getNodeForKey("meta_house_coat_of_arms"), null));
        var person = Ck3Person.fromNode(personNode, house);
        var coaMap = CoatOfArms.createCoaMap(n.getNodeForKey("coat_of_arms")
                .getNodeForKey("coat_of_arms_manager_database"));
        var titles = Ck3Title.createTitleMap(n, coaMap);

        var tagTitles = new ArrayList<Ck3Title>();
        var tagClaims = new ArrayList<Ck3Title>();
        personNode.getNodeForKeysIfExistent("landed_data", "domain").ifPresent(domain -> {
            tagTitles.addAll(domain.getNodeArray().stream()
                    .map(idNode -> titles.get(idNode.getLong()))
                    .collect(Collectors.toList()));
            personNode.getNodeForKeysIfExistent("alive_data", "claim").ifPresent(claims -> {
                tagClaims.addAll(claims.getNodeArray().stream()
                        .map(c -> titles.get(c.getNodeForKey("title").getLong()))
                        .collect(Collectors.toList()));
            });
        });


        var coa = CoatOfArms.fromNode(
                n.getNodeForKey("meta_data").getNodeForKey("meta_coat_of_arms"), null);
        var name = n.getNodeForKey("meta_data").getNodeForKey("meta_title_name").getString();

        var landedNode = personNode.getNodeForKey("landed_data");
        var gv = landedNode.getNodeForKey("government").getString();
        var existingTag = allTags.stream().filter(t -> t.id == id).findAny().orElseThrow();
        allTags.remove(existingTag);

        var tag = new Ck3Tag(id, person, tagTitles, tagClaims, gv, coa, name,
                existingTag.balance, existingTag.strength, existingTag.gold,
                existingTag.income, existingTag.piety, existingTag.prestige);
        allTags.add(tag);
        return Optional.of(tag);
    }

    public static List<Ck3Tag> fromNode(Node n) {
        var coaMap = CoatOfArms.createCoaMap(n.getNodeForKeys("coat_of_arms", "coat_of_arms_manager_database"));
        Map<Long, Ck3Title> titleIds = Ck3Title.createTitleMap(n, coaMap);

        var living = n.getNodeForKey("living");
        List<Ck3Tag> allTags = new ArrayList<>();
        living.forEach((k, v) -> {
            if (!v.hasKey("landed_data")) {
                return;
            }

            if (!v.getNodeForKey("landed_data").hasKey("domain")) {
                return;
            }

            var landedNode = v.getNodeForKey("landed_data");
            long id = Long.parseLong(k);
            var domain = v.getNodeForKey("landed_data").getNodeForKey("domain");
            if (domain.getArrayNode().size() == 0) {
                return;
            }

            var primary = domain.getNodeArray().get(0).getLong();
            var gv = v.getNodeForKey("landed_data").getNodeForKey("government").getString();
            var balance = landedNode.getNodeForKeyIfExistent("balance")
                    .map(Node::getDouble).orElse(0.0).intValue();
            var strength = landedNode.getNodeForKeyIfExistent("strength")
                    .map(Node::getDouble).orElse(0.0).intValue();

            var aliveNode = v.getNodeForKey("alive_data");
            var goldNode = aliveNode.getNodeForKeyIfExistent("gold");
            var gold = goldNode.map(node -> node.isArray() ? node.getNodeForKeyIfExistent("value")
                    .map(Node::getDouble).orElse(0.0) : node.getDouble()).orElseGet(() -> goldNode.map(Node::getDouble).orElse(0.0)).intValue();
            var income = aliveNode.getNodeForKeyIfExistent("income").map(Node::getDouble).orElse(0.0).intValue();
            var piety = aliveNode.getNodeForKeysIfExistent("piety", "currency").map(Node::getDouble).orElse(0.0).intValue();
            var prestige = aliveNode.getNodeForKeysIfExistent("prestige", "currency").map(Node::getDouble).orElse(0.0).intValue();

            var titleFromId = titleIds.get(primary);
            if (titleFromId == null) {
                return;
            }
            allTags.add(new Ck3Tag(id, null, List.of(titleFromId),
                    null, gv, null, null, balance, strength, gold, income, piety, prestige));
        });
        return allTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ck3Tag ck3Tag = (Ck3Tag) o;
        return id == ck3Tag.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Ck3Person getRuler() {
        return ruler;
    }

    public List<Ck3Title> getTitles() {
        return titles;
    }

    public List<Ck3Title> getClaims() {
        return claims;
    }

    public Ck3Title getPrimaryTitle() {
        return titles.get(0);
    }

    public long getId() {
        return id;
    }

    public String getGovernmentName() {
        return governmentName;
    }

    public CoatOfArms getCoatOfArms() {
        return coatOfArms != null ? coatOfArms : getPrimaryTitle().getCoatOfArms();
    }

    public String getName() {
        return name != null ? name : getPrimaryTitle().getName();
    }

    public int getBalance() {
        return balance;
    }

    public int getStrength() {
        return strength;
    }

    public int getGold() {
        return gold;
    }

    public int getIncome() {
        return income;
    }

    public int getPiety() {
        return piety;
    }

    public int getPrestige() {
        return prestige;
    }
}
