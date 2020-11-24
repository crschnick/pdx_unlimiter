package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public abstract class GameCampaignEntry<I extends SavegameInfo> implements Comparable<GameCampaignEntry<I>> {
    private StringProperty name;
    private UUID uuid;
    private ObjectProperty<I> info;
    private String checksum;
    private boolean persistent;

    public GameCampaignEntry(String name, UUID uuid, I info,
                            String checksum) {
        this.checksum = checksum;
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
        this.info = new SimpleObjectProperty<>(info);
        this.persistent = true;
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
}
