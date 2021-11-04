package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.info.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public abstract class SavegameCollection<T, I extends SavegameInfo<T>> {

    private final ObjectProperty<Instant> lastPlayed;
    private final StringProperty name;
    private final UUID uuid;
    private final ObservableSet<SavegameEntry<T, I>> savegames = FXCollections.observableSet(new HashSet<>());

    public SavegameCollection(Instant lastPlayed, String name, UUID uuid) {
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
    }

    public void onSavegameLoad(SavegameEntry<T, I> entry) {
    }

    public void onSavegamesChange() {
    }

    public ObservableSet<SavegameEntry<T, I>> getSavegames() {
        return savegames;
    }

    public void add(SavegameEntry<T, I> e) {
        this.savegames.add(e);
    }

    public String getName() {
        if (name.get().length() == 0) {
            return "No name";
        }

        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Stream<SavegameEntry<T, I>> entryStream() {
        var list = new ArrayList<SavegameEntry<T, I>>(getSavegames());
        list.sort(Comparator.comparing(SavegameEntry::getDate));
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
