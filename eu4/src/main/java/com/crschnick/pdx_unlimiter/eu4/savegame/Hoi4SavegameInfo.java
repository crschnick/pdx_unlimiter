package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.GameDateType;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    public static Hoi4SavegameInfo fromSavegame(Hoi4Savegame sg) throws SavegameParseException {
        Hoi4SavegameInfo i = new Hoi4SavegameInfo();
        try {
            i.tag = new Hoi4Tag(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "player")),
                    Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "ideology")));
            i.date = GameDateType.HOI4.fromNode(Node.getNodeForKey(sg.getNodes().get("gamestate"), "date"));
            i.campaignUuid = UUID.fromString(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "game_unique_id")));

            i.mods = Node.getNodeArray(Node.getNodeForKey(sg.getNodes().get("gamestate"), "mods"))
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            i.dlcs = List.of();

            Pattern p = Pattern.compile("(\\w+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)\\s+.*");
            Matcher m = p.matcher(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "version")));
            m.matches();
            i.version = new GameVersion(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)), 0, m.group(1));

        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }
}
