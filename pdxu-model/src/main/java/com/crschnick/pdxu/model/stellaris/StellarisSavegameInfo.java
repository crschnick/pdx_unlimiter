package com.crschnick.pdxu.model.stellaris;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.SavegameInfoException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StellarisSavegameInfo extends SavegameInfo<StellarisTag> {

    protected StellarisTag tag;
    protected List<StellarisTag> allTags;

    public static StellarisSavegameInfo fromSavegame(Node n) throws SavegameInfoException {
        StellarisSavegameInfo i = new StellarisSavegameInfo();
        try {
            i.ironman = n.getNodeForKey("galaxy").getNodeForKeyIfExistent("ironman")
                    .map(Node::getBoolean).orElse(false);

            i.date = GameDateType.STELLARIS.fromString(n.getNodesForKey("date").get(0).getString());

            i.binary = false;

            long seed = n.getNodeForKey("random_seed").getLong();
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

            Pattern p = Pattern.compile("(\\w*\\s*)v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
            var vs = n.getNodesForKey("version").get(0).getString();
            Matcher m = p.matcher(vs);
            if (m.matches()) {
                i.version = new GameNamedVersion(
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        m.groupCount() == 5 ? Integer.parseInt(m.group(4)) : 0, 0, m.group(1).trim());
            } else {
                throw new IllegalArgumentException("Invalid Stellaris version: " + vs);
            }
        } catch (Throwable e) {
            throw new SavegameInfoException("Could not create savegame info of savegame", e);
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
