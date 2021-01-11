package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
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

public abstract class SavegameCollection<T, I extends SavegameInfo<T>> {

    private volatile ObjectProperty<Instant> lastPlayed;
    private volatile StringProperty name;
    private UUID uuid;
    private volatile ObservableSet<GameCampaignEntry<T, I>> savegames =
            FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));

    public SavegameCollection(Instant lastPlayed, String name, UUID uuid) {
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
    }

    public ObservableSet<GameCampaignEntry<T, I>> getSavegames() {
        return savegames;
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

    public UUID getUuid() {
        return uuid;
    }

    public int indexOf(GameCampaignEntry<T, I> e) {
        return entryStream().collect(Collectors.toList()).indexOf(e);
    }

    public Stream<GameCampaignEntry<T, I>> entryStream() {
        var list = new ArrayList<GameCampaignEntry<T, I>>(getSavegames());
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
}
