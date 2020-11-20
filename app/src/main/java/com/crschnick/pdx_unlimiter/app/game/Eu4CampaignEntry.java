package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;

import java.util.UUID;

public class Eu4CampaignEntry extends GameCampaignEntry<Eu4SavegameInfo> {

    private String tag;
    private GameDate date;

    public Eu4CampaignEntry(String name, UUID uuid, Eu4SavegameInfo info, String checksum, String tag, GameDate date) {
        super(name, uuid, info, checksum);
        this.tag = tag;
        this.date = date;
    }



    public String getTag() {
        return tag;
    }

    public GameDate getDate() {
        return date;
    }

    @Override
    public int compareTo(GameCampaignEntry<Eu4SavegameInfo> o) {
        Eu4CampaignEntry e = (Eu4CampaignEntry) o;
        return e.getDate().compareTo(getDate());
    }
}
