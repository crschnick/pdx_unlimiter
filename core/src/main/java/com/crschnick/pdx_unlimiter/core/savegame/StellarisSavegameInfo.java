package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.data.GameDateType;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.data.StellarisTag;
import com.crschnick.pdx_unlimiter.core.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    public static StellarisSavegameInfo fromSavegame(StellarisSavegame sg) throws SavegameParseException {
        StellarisSavegameInfo i = new StellarisSavegameInfo();
        try {
            i.ironman = Node.getBoolean(Node.getNodeForKey(
                    Node.getNodeForKey(sg.getNodes().get("gamestate"), "galaxy"), "ironman"));

            i.date = GameDateType.STELLARIS.fromNode(Node.getNodeForKey(sg.getNodes().get("meta"), "date"));

            i.campaignUuid = UUID.randomUUID();

            int seed = Node.getInteger(Node.getNodeForKey(sg.getNodes().get("gamestate"), "random_seed"));
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            i.campaignUuid = UUID.nameUUIDFromBytes(b);

            i.allTags = new HashSet<>();
            for (Node country : Node.getNodeArray(sg.getNodes().get("countries"))) {
                KeyValueNode kv = Node.getKeyValueNode(country);

                // Invalid country node
                if (kv.getNode() instanceof ValueNode && ((ValueNode) kv.getNode()).getValue() instanceof String) {
                    continue;
                }

                Node flag = Node.getNodeForKey(kv.getNode(), "flag");
                Node icon = Node.getNodeForKey(flag, "icon");
                Node bg = Node.getNodeForKey(flag, "background");
                var tag = new StellarisTag(
                        Node.getString(Node.getNodeForKey(kv.getNode(), "name")),
                        Node.getString(Node.getNodeForKey(icon, "category")),
                        Node.getString(Node.getNodeForKey(icon, "file")),
                        Node.getString(Node.getNodeForKey(bg, "category")),
                        Node.getString(Node.getNodeForKey(bg, "file")),
                        Node.getString(Node.getNodeArray(Node.getNodeForKey(flag, "colors")).get(0)),
                        Node.getString(Node.getNodeArray(Node.getNodeForKey(flag, "colors")).get(1)));

                if (i.allTags.size() == 0) {
                    i.tag = tag;
                }

                i.allTags.add(tag);
            }

            i.mods = List.of();

            i.dlcs = Node.getNodeArray(Node.getNodeForKey(sg.getNodes().get("meta"), "required_dlcs"))
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            Pattern p = Pattern.compile("(\\w+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher m = p.matcher(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "version")));
            m.matches();
            i.version = new GameVersion(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)), 0, m.group(1));

        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    public Set<StellarisTag> getAllTags() {
        return allTags;
    }
}
