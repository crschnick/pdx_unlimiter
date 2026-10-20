package com.crschnick.pdxu.model.ck3;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.GameDateType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ck3Person {
    private GameDate birth;
    private Ck3House house;
    private String firstName;
    private List<Integer> skills;
    private List<String> traits;
    private List<Double> traitXp;
    private List<String> modifiers;
    private List<String> perks;
    private boolean female;

    public Ck3Person() {}

    public static Ck3Person fromNode(Node n, Ck3House house, List<String> traitsLookup) {
        Ck3Person p = new Ck3Person();
        if (n.hasKey("dynasty_house")) {
            p.house = house;
        }
        p.birth = GameDateType.CK3.fromString(n.getNodeForKey("birth").getString());
        p.skills = n.getNodeForKey("skill").getNodeArray().stream()
                .map(Node::getInteger)
                .collect(Collectors.toList());
        p.traits = n.getNodeForKeyIfExistent("traits").map(Node::getNodeArray).orElse(List.of()).stream()
                .map(Node::getInteger)
                .filter(i -> i >= 0 && i < traitsLookup.size())
                .map(traitsLookup::get)
                .collect(Collectors.toList());
        p.traitXp = n.getNodeForKeyIfExistent("trait_xp_amounts")
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getDouble)
                .collect(Collectors.toList());
        var alive = n.getNodeForKeyIfExistent("alive_data");
        p.modifiers = alive.map(a -> a.getNodesForKey("modifier")).orElse(List.of()).stream()
                .map(m -> m.getNodeForKeyIfExistent("modifier"))
                .flatMap(Optional::stream)
                .filter(Node::isValue)
                .map(Node::getString)
                .collect(Collectors.toList());
        p.perks = alive.flatMap(a -> a.getNodeForKeyIfExistent("perk"))
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getString)
                .collect(Collectors.toList());
        p.female = n.getNodeForKeyIfExistent("female").map(Node::getBoolean).orElse(false);
        p.firstName =
                Ck3Strings.cleanCk3FormatData(n.getNodeForKey("first_name").getString());
        return p;
    }

    public GameDate getBirth() {
        return birth;
    }

    public List<Double> getTraitXp() {
        return traitXp != null ? traitXp : List.of();
    }

    public List<String> getModifiers() {
        return modifiers != null ? modifiers : List.of();
    }

    public List<String> getPerks() {
        return perks != null ? perks : List.of();
    }

    public boolean isFemale() {
        return female;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<Integer> getSkills() {
        return skills;
    }

    public List<String> getTraits() {
        return traits != null ? traits : List.of();
    }

    public Ck3House getHouse() {
        return house;
    }
}
