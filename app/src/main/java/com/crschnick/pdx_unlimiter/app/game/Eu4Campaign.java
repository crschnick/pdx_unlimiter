package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import javafx.beans.property.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public class Eu4Campaign extends GameCampaign<Eu4CampaignEntry> implements Comparable<Eu4Campaign> {

    private volatile StringProperty tag;
    private volatile SimpleObjectProperty<GameDate> date;

    public Eu4Campaign(Instant lastPlayed, String name, UUID campaignId, String tag, GameDate date) {
        super(lastPlayed, name, campaignId);
        this.tag = new SimpleStringProperty(tag);
        this.date = new SimpleObjectProperty<>(date);
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

    @Override
    public int compareTo(Eu4Campaign o) {
        return getLastPlayed().compareTo(o.getLastPlayed());
    }
}
