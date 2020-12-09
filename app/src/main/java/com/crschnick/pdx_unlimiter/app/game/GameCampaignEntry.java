package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public final class GameCampaignEntry<T, I extends SavegameInfo> implements Comparable<GameCampaignEntry<T, I>> {

    private StringProperty name;
    private UUID uuid;
    private ObjectProperty<I> info;
    private String checksum;
    private boolean persistent;
    private GameDate date;
    private ObjectProperty<T> tag;

    public GameCampaignEntry(String name, UUID uuid, I info,
                             String checksum, GameDate date, T tag) {
        this.checksum = checksum;
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
        this.info = new SimpleObjectProperty<>(info);
        this.persistent = true;
        this.date = date;
        this.tag = new SimpleObjectProperty<>(tag);
    }

    @Override
    public int compareTo(GameCampaignEntry<T, I> o) {
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

    public String getChecksum() {
        return checksum;
    }

    public GameDate getDate() {
        return date;
    }

    public T getTag() {
        return tag.get();
    }

    public ObjectProperty<T> tagProperty() {
        return tag;
    }
}
