package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GameCampaign<T, I extends SavegameInfo<T>> {


    private volatile ObjectProperty<Instant> lastPlayed;
    private volatile StringProperty name;
    private UUID campaignId;
    private volatile ObservableSet<GameCampaignEntry<T, I>> savegames =
            FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));
    private volatile ObjectProperty<GameDate> date;
    private ObjectProperty<Image> image;

    public GameCampaign(Instant lastPlayed, String name, UUID campaignId, GameDate date, Image image) {
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.campaignId = campaignId;
        this.date = new SimpleObjectProperty<>(date);
        this.image = new SimpleObjectProperty<>(image);
    }

    public ObservableSet<GameCampaignEntry<T, I>> getSavegames() {
        return savegames;
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public void add(GameCampaignEntry<T, I> e) {
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

    public GameCampaignEntry<T, I> getLatestEntry() {
        return entryStream().findFirst().get();
    }

    public ObservableSet<GameCampaignEntry<T, I>> getEntries() {
        return savegames;
    }

    public int indexOf(GameCampaignEntry<T, I> e) {
        return entryStream().collect(Collectors.toList()).indexOf(e);
    }

    public Stream<GameCampaignEntry<T, I>> entryStream() {
        var list = new ArrayList<GameCampaignEntry<T, I>>(getEntries());
        list.sort(Comparator.comparing(GameCampaignEntry::getDate));
        Collections.reverse(list);
        return list.stream();
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
