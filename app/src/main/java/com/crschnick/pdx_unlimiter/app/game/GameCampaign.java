package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.StellarisTag;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.Instant;
import java.util.TreeSet;
import java.util.UUID;

public final class GameCampaign<T,I extends SavegameInfo<T>> {


    private volatile ObjectProperty<Instant> lastPlayed;
    private volatile StringProperty name;
    private UUID campaignId;
    private volatile ObservableSet<GameCampaignEntry<T,I>> savegames =
            FXCollections.synchronizedObservableSet(FXCollections.observableSet(new TreeSet<>()));
    private volatile ObjectProperty<GameDate> date;
    private ObjectProperty<T> tag;

    public GameCampaign(Instant lastPlayed, String name, UUID campaignId, GameDate date, T tag) {
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.campaignId = campaignId;
        this.date = new SimpleObjectProperty<>(date);
        this.tag = new SimpleObjectProperty<>(tag);
    }

    public T getTag() {
        return tag.get();
    }

    public ObjectProperty<T> tagProperty() {
        return tag;
    }

    public void add(GameCampaignEntry<T,I> e) {
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

    public GameCampaignEntry<T,I> getLatestSavegame() {
        return getSavegames().iterator().next();
    }

    public ObservableSet<GameCampaignEntry<T,I>> getSavegames() {
        return savegames;
    }

    public Instant getLastPlayed() {
        return lastPlayed.get();
    }

    public ObjectProperty<Instant> lastPlayedProperty() {
        return lastPlayed;
    }

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
    }
}
