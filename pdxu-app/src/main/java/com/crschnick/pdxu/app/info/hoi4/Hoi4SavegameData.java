package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("hoi4")
public class Hoi4SavegameData extends SavegameData<Hoi4Tag> {

    protected Hoi4Tag tag;
    protected List<Hoi4Tag> allTags;
    private GameNamedVersion version;

    @Override
    public Hoi4Tag getTag() {
        return tag;
    }

    @Override
    public GameNamedVersion getVersion() {
        return version;
    }

    @Override
    public List<Hoi4Tag> getAllTags() {
        return allTags;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.HOI4.getCampaignIdHeuristic(content);
        tag = new Hoi4Tag(content.get().getNodeForKey("player").getString(), content.get().getNodeForKey("ideology").getString());
        date = GameDateType.HOI4.fromString(content.get().getNodeForKey("date").getString());
        ironman = content.get().hasKey("Ironman");
        allTags = List.of(tag);
        mods = content.get().getNodeForKeyIfExistent("mods")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
              .collect(Collectors.toCollection(LinkedHashSet::new));
        dlcs = null;
        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("([\\w ]+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\w+))?\\s+.*");
        var string = n.getNodeForKey("version").getString();
        Matcher m = p.matcher(string);
        if (m.matches()) {
            version = new GameNamedVersion(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)),
                                             Integer.parseInt(m.group(4)), 0, m.group(1));
        } else {
            throw new IllegalArgumentException("Could not parse HOI4 version string: " + string);
        }
    }
}
