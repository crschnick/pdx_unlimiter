package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.parser.ParseException;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.eu5.Eu5Tag;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("eu5")
@Getter
public class Eu5SavegameData extends SavegameData<Eu5Tag> {

    private String name;
    private Eu5Tag tag;
    private List<Eu5Tag> allTags;
    private GameVersion version;
    private CoatOfArms tagCoa;

    @Override
    public Eu5Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    public List<Eu5Tag> getAllTags() {
        return allTags;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.EU5.getCampaignIdHeuristic(content);
        allTags = Eu5Tag.allFromNode(content.get());

        var countryId =
                NodePointer.builder().name("played_country").name("country").build();
        var tagId = countryId.get(content.get());
        tag = tagId != null ? Eu5Tag.getTag(allTags, tagId.getLong()) : null;

        tagCoa = content.get()
                .getNodeForKeysIfExistent("metadata", "flag")
                .map(Node::getString)
                .map(s -> {
                    var toParse = s.getBytes(StandardCharsets.UTF_8);
                    try {
                        var parsed = TextFormatParser.eu5().parse("flag", toParse, 0, false);
                        return parsed.size() == 1
                                ? CoatOfArms.fromNode(parsed.getNodeArray().getFirst(), parent -> null)
                                : null;
                    } catch (ParseException e) {
                        return null;
                    }
                })
                .orElse(null);
        date = GameDateType.EU5.fromString(
                content.get().getNodeForKeys("metadata", "date").getString());
        mods = content.get().getNodeForKeyIfExistent("mods").map(Node::getNodeArray).orElse(List.of()).stream()
                .map(Node::getString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        dlcs = content
                .get()
                .getNodeForKey("metadata")
                .getNodeForKeyIfExistent("enabled_dlcs")
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getString)
                .collect(Collectors.toList());
        name = content.get().getNodeForKeys("metadata").hasKey("player_country_name")
                ? content.get()
                        .getNodeForKey("metadata")
                        .getNodeForKey("player_country_name")
                        .getString()
                : content.get()
                        .getNodeForKey("metadata")
                        .getNodeForKey("playthrough_name")
                        .getString();
        ironman = content.get()
                .getNodeForKeysIfExistent("metadata", "ironman")
                .map(Node::getBoolean)
                .orElse(false);
        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        var string = n.getNodeForKeys("metadata", "version").getString();
        Matcher m = p.matcher(string);
        if (m.matches()) {
            version = new GameVersion(
                    Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 0);
        } else {
            throw new IllegalArgumentException("Could not parse EU5 version string: " + string);
        }
    }
}
