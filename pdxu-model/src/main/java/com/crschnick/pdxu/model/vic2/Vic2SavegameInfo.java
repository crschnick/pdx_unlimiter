package com.crschnick.pdxu.model.vic2;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Vic2SavegameInfo extends SavegameInfo<Vic2Tag> {

    private Vic2Tag tag;
    private List<Vic2Tag> allTags;
    private GameVersion version;

    public Vic2SavegameInfo() {
    }

    public Vic2SavegameInfo(Node n) throws SavegameInfoException {
        try {
            ironman = false;
            date = GameDateType.VIC2.fromString(n.getNodesForKey("date").get(0).getString());
            binary = false;

            allTags = new ArrayList<>();
            n.forEach((k, v) -> {
                if (!k.toUpperCase().equals(k) || k.length() != 3) {
                    return;
                }

                allTags.add(new Vic2Tag(k));
            });
            var playerTag = n.getNodeForKey("player").getString();
            tag = allTags.stream().filter(t -> t.getTagId().equals(playerTag))
                    .findAny()
                    .orElseThrow(() -> new SavegameInfoException("No player tag found"));

            mods = null;
            dlcs = List.of();

            // Hardcode version
            version = new GameVersion(3, 4, 0, 0);

            if (!n.hasKey("previous_war")) {
                throw new SavegameInfoException("Can't determine campaign when no previous wars are present.");
            } else {
                campaignHeuristic = UUID.nameUUIDFromBytes(NodeWriter.writeToBytes(
                        (ArrayNode) n.getNodeForKey("previous_war"), Integer.MAX_VALUE, ""));
            }
        } catch (SavegameInfoException e) {
            throw e;
        } catch (Throwable e) {
            throw new SavegameInfoException("Could not create savegame info of savegame", e);
        }
    }

    @Override
    public Vic2Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    public List<Vic2Tag> getAllTags() {
        return allTags;
    }
}
