package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.data.Eu4Date;
import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;

import java.util.UUID;

public class Eu4CampaignEntry extends GameCampaignEntry<Eu4SavegameInfo> {

    private String tag;
    private Eu4Date date;

    public Eu4CampaignEntry(String name, UUID uuid, Eu4SavegameInfo info, String checksum, String tag, Eu4Date date) {
        super(name, uuid, info, checksum);
        this.tag = tag;
        this.date = date;
    }


    public String getTag() {
        return tag;
    }

    public Eu4Date getDate() {
        return date;
    }

    @Override
    public int compareTo(GameCampaignEntry<Eu4SavegameInfo> o) {
        Eu4CampaignEntry e = (Eu4CampaignEntry) o;
        return e.getDate().compareTo(getDate());
    }
}
