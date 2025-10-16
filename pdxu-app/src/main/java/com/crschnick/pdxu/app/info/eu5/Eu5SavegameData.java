package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.eu5.Eu5Tag;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

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
        var previous = content.get().getNodeForKey("previous_played").getArrayNode().getNodeArray().getFirst();
        var tagId = previous.getNodeForKey("idtype").getInteger();
        tag = Eu5Tag.getTag(allTags, tagId);
        date = GameDateType.EU5.fromString(content.get().getNodeForKeys("ironman_manager", "date").getString());
        mods = content.get().getNodeForKeyIfExistent("mods").map(Node::getNodeArray).orElse(List.of()).stream()
                .map(Node::getString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        dlcs = null;
        name = content.get().getNodeForKeys("metadata", "player_country_name").getString();
        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
        var string = n.getNodeForKeys("metadata", "version").getString();
        Matcher m = p.matcher(string);
        if (m.matches()) {
            version = new GameVersion(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    0);
        } else {
            throw new IllegalArgumentException("Could not parse EU5 version string: " + string);
        }
    }
}
