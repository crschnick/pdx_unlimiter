package com.crschnick.pdx_unlimiter.core.info.ck2;

import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfoException;
import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ck2SavegameInfo extends SavegameInfo<Ck2Tag> {

    private Ck2Tag tag;
    private List<Ck2Tag> allTags;

    public Ck2SavegameInfo() {
    }

    public Ck2SavegameInfo(Node n) throws SavegameInfoException {
        try {
            ironman = false;
            date = GameDateType.CK3.fromString(n.getNodesForKey("date").get(0).getString());
            binary = false;

            long seed = n.getNodeForKey("playthrough_id").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            campaignHeuristic = UUID.nameUUIDFromBytes(b);

            allTags = new ArrayList<>();

            mods = List.of();

            dlcs = List.of();

            Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
            var vs = n.getNodesForKey("version").get(0).getString();
            Matcher m = p.matcher(vs);
            if (m.matches()) {
                version = new GameVersion(
                        Integer.parseInt(m.group(1)),
                        Integer.parseInt(m.group(2)),
                        Integer.parseInt(m.group(3)),
                        Integer.parseInt(m.group(4)),
                        null);
            } else {
                throw new IllegalArgumentException("Invalid CK2 version: " + vs);
            }
        } catch (Throwable e) {
            throw new SavegameInfoException("Could not create savegame info of savegame", e);
        }
    }

    @Override
    public Ck2Tag getTag() {
        return tag;
    }

    @Override
    public List<Ck2Tag> getAllTags() {
        return allTags;
    }
}
