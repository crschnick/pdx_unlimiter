package com.crschnick.pdxu.model.ck3;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.GameDateType;

import java.util.List;
import java.util.stream.Collectors;

public class Ck3Person {
    private GameDate birth;
    private Ck3House house;
    private String firstName;
    private List<Integer> skills;

    public Ck3Person() {
    }

    public static Ck3Person fromNode(Node n, Ck3House house) {
        Ck3Person p = new Ck3Person();
        if (n.hasKey("dynasty_house")) {
            p.house = house;
        }
        p.birth = GameDateType.CK3.fromString(n.getNodeForKey("birth").getString());
        p.skills = n.getNodeForKey("skill").getNodeArray().stream()
                .map(Node::getInteger)
                .collect(Collectors.toList());
        p.firstName = Ck3Strings.cleanCk3FormatData(n.getNodeForKey("first_name").getString());
        return p;
    }

    public GameDate getBirth() {
        return birth;
    }

    public String getFirstName() {
        return firstName;
    }

    public List<Integer> getSkills() {
        return skills;
    }

    public Ck3House getHouse() {
        return house;
    }
}