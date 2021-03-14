package com.crschnick.pdx_unlimiter.core.info.hoi4;

import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParseException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    protected Hoi4Tag tag;
    protected Set<Hoi4Tag> allTags;

    public static Hoi4SavegameInfo fromSavegame(boolean melted, Node n) throws SavegameParseException {
        Hoi4SavegameInfo i = new Hoi4SavegameInfo();
        try {
            i.tag = new Hoi4Tag(n.getNodeForKey("player").getString(), n.getNodeForKey("ideology").getString());
            i.date = GameDateType.HOI4.fromString(n.getNodeForKey("date").getString());
            i.campaignHeuristic = UUID.fromString(n.getNodeForKey("game_unique_id").getString());
            i.ironman = melted;
            i.binary = melted;

            i.mods = n.getNodeForKeyIfExistent("mods")
                    .map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            i.dlcs = List.of();

            Pattern p = Pattern.compile("(\\w+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)\\s+.*");
            Matcher m = p.matcher(n.getNodeForKey("version").getString());
            m.matches();
            i.version = new GameVersion(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)), 0, m.group(1));

        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    @Override
    public Hoi4Tag getTag() {
        return tag;
    }

    @Override
    public Set<Hoi4Tag> getAllTags() {
        return allTags;
    }
}
