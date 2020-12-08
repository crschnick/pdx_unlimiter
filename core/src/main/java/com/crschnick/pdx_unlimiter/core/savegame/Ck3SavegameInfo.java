package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Ck3SavegameInfo extends SavegameInfo<Ck3Tag> {

    public static Ck3SavegameInfo fromSavegame(Ck3Savegame sg) throws SavegameParseException {
        Ck3SavegameInfo i = new Ck3SavegameInfo();
        try {
            i.ironman = Node.getBoolean(Node.getNodeForKey(sg.getNodes().get("meta"), "ironman"));
            i.date = GameDateType.CK3.fromNode(Node.getNodeForKey(sg.getNodes().get("gamestate"), "date"));

            long seed = Node.getLong(Node.getNodeForKey(sg.getNodes().get("gamestate"), "random_seed"));
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            i.campaignUuid = UUID.nameUUIDFromBytes(b);

            i.allTags = Ck3Tag.fromNode(
                    sg.getNodes().get("living"),
                    sg.getNodes().get("landed_titles"),
                    sg.getNodes().get("coat_of_arms"));
            i.tag = Ck3Tag.getPlayerTag(sg.getNodes().get("gamestate"), i.allTags);

            i.mods = Node.getNodeArray(Node.getNodeForKey(sg.getNodes().get("meta"), "mods"))
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());
            i.dlcs = List.of();

            Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher m = p.matcher(Node.getString(Node.getNodeForKey(sg.getNodes().get("meta"), "version")));
            m.matches();
            i.version = new GameVersion(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), 0, "?");

        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }
}
