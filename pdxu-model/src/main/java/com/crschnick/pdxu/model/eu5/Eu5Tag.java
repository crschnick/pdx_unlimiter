package com.crschnick.pdxu.model.eu5;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Value
@Builder
@Jacksonized
public class Eu5Tag {

    public static final String INVALID_TAG_ID = "DUMMY";

    long id;
    String flagTag;
    String nameTag;
    GameColor color;

    public static List<Eu5Tag> allFromNode(Node n) {
        var countries = n.getNodeForKeys("countries", "database");
        var allTags = new ArrayList<Eu5Tag>();
        countries.forEach((s, node) -> {
            allTags.add(fromNode(s, node));
        });
        return allTags;
    }

    public static Eu5Tag fromNode(String key, Node n) {
        if (!n.isArray()) {
            return new Eu5Tag(Integer.parseInt(key), INVALID_TAG_ID, INVALID_TAG_ID, GameColor.BLACK);
        }

        var color = n.getNodeForKeysIfExistent("color")
                .map(GameColor::fromColorNode)
                .orElse(GameColor.BLACK);
        var flagTag = n.getNodeForKey("flag").getString();
        var nameTag = n.getNodeForKey("country_name").isValue() ? n.getNodeForKey("country_name").getString() :
                n.getNodeForKey("country_name").getNodeForKey("name").getString();
        return new Eu5Tag(Long.parseLong(key), flagTag, nameTag, color);
    }

    public static Eu5Tag getTag(List<Eu5Tag> tags, long id) {
        return tags.stream()
                .filter(t -> t.id == id)
                .findFirst()
                .orElse(new Eu5Tag(0, INVALID_TAG_ID, INVALID_TAG_ID, GameColor.BLACK));
    }
}
