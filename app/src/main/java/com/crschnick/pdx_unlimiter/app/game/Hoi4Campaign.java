package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import com.crschnick.pdx_unlimiter.eu4.parser.Hoi4Date;
import com.crschnick.pdx_unlimiter.eu4.parser.Hoi4Tag;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Instant;
import java.util.UUID;

public class Hoi4Campaign extends GameCampaign<Hoi4CampaignEntry> implements Comparable<Hoi4Campaign> {

    private volatile SimpleObjectProperty<Hoi4Tag> tag;
    private volatile SimpleObjectProperty<Hoi4Date> date;

    public Hoi4Campaign(Instant lastPlayed, String name, UUID campaignId, Hoi4Tag tag, Hoi4Date date) {
        super(lastPlayed, name, campaignId);
        this.tag = new SimpleObjectProperty<>(tag);
        this.date = new SimpleObjectProperty<>(date);
    }

    public Hoi4Date getDate() {
        return date.get();
    }

    public SimpleObjectProperty<Hoi4Date> dateProperty() {
        return date;
    }

    public Hoi4Tag getTag() {
        return tag.get();
    }

    public SimpleObjectProperty<Hoi4Tag> tagProperty() {
        return tag;
    }

    @Override
    public int compareTo(Hoi4Campaign o) {
        return getLastPlayed().compareTo(o.getLastPlayed());
    }
}