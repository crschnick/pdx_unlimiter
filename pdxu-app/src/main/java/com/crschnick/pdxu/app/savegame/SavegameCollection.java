package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.model.SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public abstract class SavegameCollection<T, I extends SavegameInfo<T>> {

    protected final SavegameStorage<T,I> storage;
    private final ObjectProperty<Instant> lastPlayed;
    private final StringProperty name;
    private final UUID uuid;
    protected final ObservableSet<SavegameEntry<T, I>> savegames = FXCollections.observableSet(new HashSet<>());

    public SavegameCollection(SavegameStorage<T, I> storage, Instant lastPlayed, String name, UUID uuid) {
        this.storage = storage;
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
    }

    public abstract void addNewEntry(
            String checksum,
            I info,
            String name,
            String sourceFileChecksum);

    public final Path getDirectory() {
        return storage.getSavegameDataDirectory().resolve(getUuid().toString());
    }

    public abstract void deserializeEntries() throws Exception;

    public abstract void saveData();

    public abstract JsonNode serialize();

    public abstract void copyTo(SavegameStorage<T,I> storage, SavegameEntry<T,I> entry);

    public abstract String getOutputName(String fileName, String entryName);

    public void onSavegameLoad(SavegameEntry<T, I> entry) {
    }

    public void onSavegamesChange() {
    }

    public ObservableSet<SavegameEntry<T, I>> getSavegames() {
        return savegames;
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
