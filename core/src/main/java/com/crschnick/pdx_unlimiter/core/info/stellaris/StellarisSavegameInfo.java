package com.crschnick.pdx_unlimiter.core.info.stellaris;

import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.node.Node;
import com.crschnick.pdx_unlimiter.core.parser.ParseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    protected StellarisTag tag;
    protected List<StellarisTag> allTags;

    public static StellarisSavegameInfo fromSavegame(Node n) throws ParseException {
        StellarisSavegameInfo i = new StellarisSavegameInfo();
        try {
            i.ironman = n.getNodeForKey("galaxy").getNodeForKeyIfExistent("ironman")
                    .map(Node::getBoolean).orElse(false);

            i.date = GameDateType.STELLARIS.fromString(n.getNodesForKey("date").get(0).getString());

            i.binary = false;

            int seed = n.getNodeForKey("random_seed").getInteger();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            i.campaignHeuristic = UUID.nameUUIDFromBytes(b);

            i.allTags = new ArrayList<>();
            n.getNodeForKey("country").forEach((k, v) -> {
                // Invalid country node
                if (v.isValue()) {
                    return;
                }

                Node flag = v.getNodeForKey("flag");
                Node icon = flag.getNodeForKey("icon");
                Node bg = flag.getNodeForKey("background");
                var tag = new StellarisTag(
                        v.getNodeForKey("name").getString(),
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
            });

            i.mods = List.of();

            i.dlcs = n.getNodeForKeyIfExistent("required_dlcs").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());

            Pattern p = Pattern.compile("(\\w+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)");
            Matcher m = p.matcher(n.getNodesForKey("version").get(0).getString());
            m.matches();
            i.version = new GameVersion(Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)), 0, m.group(1));

        } catch (Exception e) {
            throw new ParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    @Override
    public StellarisTag getTag() {
        return tag;
    }

    public List<StellarisTag> getAllTags() {
        return allTags;
    }
}
