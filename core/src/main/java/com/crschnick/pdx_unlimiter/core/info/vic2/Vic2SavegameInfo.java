package com.crschnick.pdx_unlimiter.core.info.vic2;

import com.crschnick.pdx_unlimiter.core.info.GameDateType;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfoException;
import com.crschnick.pdx_unlimiter.core.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Vic2SavegameInfo extends SavegameInfo<Vic2Tag> {

    private Vic2Tag tag;
    private List<Vic2Tag> allTags;

    public Vic2SavegameInfo() {
    }

    public Vic2SavegameInfo(Node n) throws SavegameInfoException {
        try {
            ironman = false;
            date = GameDateType.CK3.fromString(n.getNodesForKey("date").get(0).getString());
            binary = false;

            long seed = n.getNodeForKey("state").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            campaignHeuristic = UUID.nameUUIDFromBytes(b);

            allTags = new ArrayList<>();

            mods = List.of();

            dlcs = List.of();

            version = new GameVersion(0, 0, 0, 0, null);
        } catch (Throwable e) {
            throw new SavegameInfoException("Could not create savegame info of savegame", e);
        }
    }

    @Override
    public Vic2Tag getTag() {
        return tag;
    }

    @Override
    public List<Vic2Tag> getAllTags() {
        return allTags;
    }
}
