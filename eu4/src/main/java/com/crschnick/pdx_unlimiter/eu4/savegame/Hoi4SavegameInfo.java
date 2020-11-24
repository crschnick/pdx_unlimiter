package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.data.Hoi4Date;
import com.crschnick.pdx_unlimiter.eu4.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.UUID;

public class Hoi4SavegameInfo extends SavegameInfo {

    private Hoi4Tag tag;
    private Hoi4Date date;
    private UUID campaignUuid;
    private Hoi4Savegame savegame;

    public static Hoi4SavegameInfo fromSavegame(Hoi4Savegame sg) throws SavegameParseException {
        Hoi4SavegameInfo i = new Hoi4SavegameInfo();
        try {
            i.tag = new Hoi4Tag(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "player")),
                    Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "ideology")));
            i.date = Hoi4Date.fromNode(Node.getNodeForKey(sg.getNodes().get("gamestate"), "date"));
            i.campaignUuid = UUID.fromString(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "game_unique_id")));
            i.savegame = sg;
        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }

    public Hoi4Tag getTag() {
        return tag;
    }

    public Hoi4Date getDate() {
        return date;
    }

    public UUID getCampaignUuid() {
        return campaignUuid;
    }

    public Hoi4Savegame getSavegame() {
        return savegame;
    }
}
