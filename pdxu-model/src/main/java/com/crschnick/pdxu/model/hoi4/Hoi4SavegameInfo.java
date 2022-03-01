package com.crschnick.pdxu.model.hoi4;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.*;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    protected Hoi4Tag tag;
    protected List<Hoi4Tag> allTags;
    private GameNamedVersion version;

    public static Hoi4SavegameInfo fromSavegame(boolean melted, SavegameContent c) throws SavegameInfoException {
        Hoi4SavegameInfo i = new Hoi4SavegameInfo();
        try {
            Node n = c.get();

            i.campaignHeuristic = SavegameType.HOI4.getCampaignIdHeuristic(c);

            i.tag = new Hoi4Tag(n.getNodeForKey("player").getString(), n.getNodeForKey("ideology").getString());
            i.date = GameDateType.HOI4.fromString(n.getNodeForKey("date").getString());
            i.ironman = melted;
            i.binary = melted;

            i.mods = n.getNodeForKeyIfExistent("mods")
                    .map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            i.dlcs = List.of();

            Pattern p = Pattern.compile("(\\w+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\w+))?\\s+.*");
            Matcher m = p.matcher(n.getNodeForKey("version").getString());
            m.matches();
            i.version = new GameNamedVersion(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4)), 0, m.group(1));

        } catch (Throwable e) {
            throw new SavegameInfoException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    @Override
    public Hoi4Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    public List<Hoi4Tag> getAllTags() {
        return allTags;
    }
}
