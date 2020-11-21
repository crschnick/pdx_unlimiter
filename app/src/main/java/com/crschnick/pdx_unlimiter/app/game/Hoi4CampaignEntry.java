package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.parser.Hoi4Date;
import com.crschnick.pdx_unlimiter.eu4.parser.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.savegame.Hoi4SavegameInfo;
import javafx.beans.property.SimpleObjectProperty;

import java.util.UUID;

public class Hoi4CampaignEntry extends GameCampaignEntry<Hoi4SavegameInfo> {

    private volatile Hoi4Tag tag;
    private volatile Hoi4Date date;

    public Hoi4CampaignEntry(String name, UUID uuid, Hoi4SavegameInfo info, String checksum, Hoi4Tag tag, Hoi4Date date) {
        super(name, uuid, info, checksum);
        this.tag = tag;
        this.date = date;
    }

    @Override
    public int compareTo(GameCampaignEntry<Hoi4SavegameInfo> o) {
        Hoi4CampaignEntry e = (Hoi4CampaignEntry) o;
        return date.compareTo(e.date);
    }

    public Hoi4Tag getTag() {
        return tag;
    }

    public Hoi4Date getDate() {
        return date;
    }
}
