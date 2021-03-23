package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SavegameEntry<T, I extends SavegameInfo<T>> implements Comparable<SavegameEntry<T, I>> {

    private final StringProperty name;
    private final UUID uuid;
    private final ObjectProperty<I> info;
    private final String contentChecksum;
    private final GameDate date;
    private final SavegameNotes notes;
    private final List<String> sourceFileChecksums;

    public SavegameEntry(String name, UUID uuid, I info,
                         String contentChecksum, GameDate date, SavegameNotes notes,
                         List<String> sourceFileChecksums) {
        this.contentChecksum = contentChecksum;
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
        this.info = new SimpleObjectProperty<>(info);
        this.date = date;
        this.notes = notes;
        this.sourceFileChecksums = new ArrayList<>(sourceFileChecksums);
    }

    @Override
    public int compareTo(SavegameEntry<T, I> o) {
        return o.getDate().compareTo(getDate());
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

    public I getInfo() {
        return info.get();
    }

    public ObjectProperty<I> infoProperty() {
        return info;
    }

    public String getContentChecksum() {
        return contentChecksum;
    }

    public GameDate getDate() {
        return date;
    }

    public List<String> getSourceFileChecksums() {
        return sourceFileChecksums;
    }

    public void addSourceFileChecksum(String sourceFileChecksum) {
        this.sourceFileChecksums.add(sourceFileChecksum);
    }

    public SavegameNotes getNotes() {
        return notes;
    }
}
