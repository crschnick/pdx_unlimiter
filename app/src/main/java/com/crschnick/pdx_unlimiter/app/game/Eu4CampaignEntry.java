package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;
import java.util.UUID;

public class Eu4CampaignEntry extends GameCampaignEntry<Eu4SavegameInfo> {

    private String tag;
    private GameDate date;

    public Eu4CampaignEntry(StringProperty name, UUID uuid, ObjectProperty<Optional<Eu4SavegameInfo>> info, String tag, GameDate date) {
        super(name, uuid, info);
        this.tag = tag;
        this.date = date;
    }

    public String getTag() {
        return tag;
    }

    public GameDate getDate() {
        return date;
    }
}
