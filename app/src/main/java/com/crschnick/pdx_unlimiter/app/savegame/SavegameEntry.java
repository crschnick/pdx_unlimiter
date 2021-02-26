package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public final class SavegameEntry<T, I extends SavegameInfo<T>> implements Comparable<SavegameEntry<T, I>> {

    private StringProperty name;
    private UUID uuid;
    private ObjectProperty<I> info;
    private String contentChecksum;
    private boolean persistent;
    private GameDate date;
    private String sourceFileChecksum;

    public SavegameEntry(String name, UUID uuid, I info,
                         String contentChecksum, GameDate date) {
        this.contentChecksum = contentChecksum;
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
        this.info = new SimpleObjectProperty<>(info);
        this.persistent = true;
        this.date = date;
    }

    @Override
    public int compareTo(SavegameEntry<T, I> o) {
        return o.getDate().compareTo(getDate());
    }

    public boolean isPersistent() {
        return persistent;
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
}
