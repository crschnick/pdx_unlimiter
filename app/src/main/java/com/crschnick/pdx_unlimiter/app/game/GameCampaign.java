package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

public class GameCampaign<E extends GameCampaignEntry<? extends SavegameInfo>> {


    private volatile ObjectProperty<Instant> lastPlayed;
    private volatile StringProperty name;
    private UUID campaignId;
    private volatile ObservableSet<E> savegames =
            FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));

    public GameCampaign(Instant lastPlayed,String name, UUID campaignId) {
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.campaignId = campaignId;
    }

    public void add(E e) {
        this.savegames.add(e);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public ObservableSet<E> getSavegames() {
        return savegames;
    }

    public Instant getLastPlayed() {
        return lastPlayed.get();
    }

    public ObjectProperty<Instant> lastPlayedProperty() {
        return lastPlayed;
    }

}
