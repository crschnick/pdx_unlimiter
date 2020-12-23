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

    public static StellarisSavegameInfo fromSavegame(Node n) throws SavegameParseException {
        StellarisSavegameInfo i = new StellarisSavegameInfo();
        try {
            i.ironman = n.getNodeForKey("galaxy").getNodeForKey("ironman").getBoolean();

            i.date = GameDateType.STELLARIS.fromString(n.getNodeForKey("date").getString());

            i.campaignUuid = UUID.randomUUID();

            int seed = n.getNodeForKey("random_seed").getInteger();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            i.campaignUuid = UUID.nameUUIDFromBytes(b);

            i.allTags = new HashSet<>();
            for (Node country : n.getNodeForKey("countries").getNodeArray()) {
                KeyValueNode kv = country.getKeyValueNode();

                // Invalid country node
                if (kv.getNode() instanceof ValueNode && ((ValueNode) kv.getNode()).getValue() instanceof String) {
                    continue;
                }

                Node flag = kv.getNode().getNodeForKey("flag");
                Node icon = flag.getNodeForKey("icon");
                Node bg = flag.getNodeForKey("background");
                var tag = new StellarisTag(
                        kv.getNode().getNodeForKey("name").getString(),
                        icon.getNodeForKey("category").getString(),
                        icon.getNodeForKey("file").getString(),
                        bg.getNodeForKey("category").getString(),
                        bg.getNodeForKey("file").getString(),
                        flag.getNodeForKey("colors").getNodeArray().get(0).getString(),
                        flag.getNodeForKey("colors").getNodeArray().get(1).getString());

                if (i.allTags.size() == 0) {
                    i.tag = tag;
                }

                i.allTags.add(tag);
            }

            i.mods = List.of();

            i.dlcs = n.getNodeForKey("required_dlcs").getNodeArray()
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            Pattern p = Pattern.compile("(\\w+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher m = p.matcher(n.getNodeForKey("version").getString());
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
