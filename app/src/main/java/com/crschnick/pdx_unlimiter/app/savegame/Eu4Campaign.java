package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.eu4.parser.GameDate;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class Eu4Campaign {

    private volatile ObjectProperty<Timestamp> lastPlayed;
    private volatile StringProperty tag;
    private volatile StringProperty name;
    private volatile ObjectProperty<GameDate> date;
    private UUID campaignId;
    private volatile BooleanProperty isLoaded = new SimpleBooleanProperty(false);
    private volatile ObservableSet<Entry> savegames =
            FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));

    public Eu4Campaign(ObjectProperty<Timestamp> lastPlayed, StringProperty tag, StringProperty name, ObjectProperty<GameDate> date, UUID campaignId) {
        this.lastPlayed = lastPlayed;
        this.tag = tag;
        this.name = name;
        this.date = date;
        this.campaignId = campaignId;
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

    public static class Entry {
        private StringProperty name;
        private UUID uuid;
        private Eu4Campaign campaign;
        private ObjectProperty<Optional<Eu4SavegameInfo>> info;

        public Entry(StringProperty name, UUID uuid, Eu4Campaign campaign) {
            this.name = name;
            this.uuid = uuid;
            this.campaign = campaign;
            this.info = new SimpleObjectProperty<>(Optional.empty());
        }

        public Entry(StringProperty name, UUID uuid, Eu4Campaign campaign, Eu4SavegameInfo info) {
            this.name = name;
            this.uuid = uuid;
            this.campaign = campaign;
            this.info = new SimpleObjectProperty<>(Optional.of(info));
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

        public Optional<Eu4SavegameInfo> getInfo() {
            return info.get();
        }

        public Optional<GameDate> getDate() {
            if (info.get().isPresent()) {
                return Optional.ofNullable(info.get().get().getDate());
            } else {
                return Optional.empty();
            }
        }

        public ObjectProperty<Optional<Eu4SavegameInfo>> infoProperty() {
            return info;
        }
    }
}
