package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import javafx.beans.property.*;

import java.sql.Timestamp;
import java.util.UUID;

public class Eu4Campaign extends GameCampaign<Eu4CampaignEntry> {

    private volatile StringProperty tag;
    private volatile SimpleObjectProperty<GameDate> date;

    public Eu4Campaign(ObjectProperty<Timestamp> lastPlayed, StringProperty name, UUID campaignId, StringProperty tag, SimpleObjectProperty<GameDate> date) {
        super(lastPlayed, name, campaignId);
        this.tag = tag;
        this.date = date;
    }

    public GameDate getDate() {
        return date.get();
    }

    public SimpleObjectProperty<GameDate> dateProperty() {
        return date;
    }

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

}
