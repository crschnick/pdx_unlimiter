package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.GameDateType;
import com.crschnick.pdx_unlimiter.eu4.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;

import java.util.UUID;

public class Hoi4SavegameInfo extends SavegameInfo<Hoi4Tag> {

    public static Hoi4SavegameInfo fromSavegame(Hoi4Savegame sg) throws SavegameParseException {
        Hoi4SavegameInfo i = new Hoi4SavegameInfo();
        try {
            i.tag = new Hoi4Tag(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "player")),
                    Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "ideology")));
            i.date = GameDateType.HOI4.fromNode(Node.getNodeForKey(sg.getNodes().get("gamestate"), "date"));
            i.campaignUuid = UUID.fromString(Node.getString(Node.getNodeForKey(sg.getNodes().get("gamestate"), "game_unique_id")));
        } catch (Exception e) {
            throw new SavegameParseException("Could not create savegame info of savegame", e);
        }
        return i;
    }
}
