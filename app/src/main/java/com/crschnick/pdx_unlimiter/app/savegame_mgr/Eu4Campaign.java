package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.sql.Timestamp;
import java.util.*;

public class Eu4Campaign implements Comparable<Eu4Campaign> {

    public static class Entry implements Comparable<Entry> {
        private StringProperty name;
        private UUID uuid;
        private Eu4Campaign campaign;
        private Eu4SavegameInfo info;

        public Entry(StringProperty name, UUID uuid, Eu4Campaign campaign, Eu4SavegameInfo info) {
            this.name = name;
            this.uuid = uuid;
            this.campaign = campaign;
            this.info = info;
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public UUID getUuid() {
            return uuid;
        }

        public Eu4Campaign getCampaign() {
            return campaign;
        }

        public Eu4SavegameInfo getInfo() {
            return info;
        }

        @Override
        public int compareTo(Entry o) {
            return info.getDate().compareTo(o.info.getDate());
        }
    }

    private ObjectProperty<Timestamp> lastPlayed;
    private StringProperty tag;
    private StringProperty name;
    private ObjectProperty<GameDate> date;
    private UUID campaignId;
    private BooleanProperty isLoaded = new SimpleBooleanProperty(false);
    private volatile ObservableSet<Entry> savegames = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new TreeSet<>()));

    public Eu4Campaign(ObjectProperty<Timestamp> lastPlayed, StringProperty tag, StringProperty name, ObjectProperty<GameDate> date, UUID campaignId) {
        this.lastPlayed = lastPlayed;
        this.tag = tag;
        this.name = name;
        this.date = date;
        this.campaignId = campaignId;
    }

    @Override
    public int compareTo(Eu4Campaign o) {
        return this.lastPlayed.get().compareTo(o.lastPlayed.get());
    }

    public void add(Entry e) {
        this.savegames.add(e);
    }

    public String getTag() {
        return tag.get();
    }

    public StringProperty tagProperty() {
        return tag;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public boolean isIsLoaded() {
        return isLoaded.get();
    }

    public BooleanProperty isLoadedProperty() {
        return isLoaded;
    }

    public ObservableSet<Entry> getSavegames() {
        return savegames;
    }

    public Timestamp getLastPlayed() {
        return lastPlayed.get();
    }

    public ObjectProperty<Timestamp> lastPlayedProperty() {
        return lastPlayed;
    }
}
