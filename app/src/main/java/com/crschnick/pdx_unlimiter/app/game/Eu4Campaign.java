package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.data.Eu4Date;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;
import java.util.UUID;

public class Eu4Campaign extends GameCampaign<Eu4CampaignEntry> implements Comparable<Eu4Campaign> {

    private volatile StringProperty tag;
    private volatile SimpleObjectProperty<Eu4Date> date;

    public Eu4Campaign(Instant lastPlayed, String name, UUID campaignId, String tag, Eu4Date date) {
        super(lastPlayed, name, campaignId);
        this.tag = new SimpleStringProperty(tag);
        this.date = new SimpleObjectProperty<>(date);
    }

    public Eu4Date getDate() {
        return date.get();
    }

    public SimpleObjectProperty<Eu4Date> dateProperty() {
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
