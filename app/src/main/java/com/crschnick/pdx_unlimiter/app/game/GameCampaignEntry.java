package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;
import java.util.UUID;

public class GameCampaignEntry<I extends SavegameInfo> {
    private StringProperty name;
    private UUID uuid;
    private ObjectProperty<I> info;

    public GameCampaignEntry(String name, UUID uuid, I info) {
        this.name = new SimpleStringProperty(name);
        this.uuid = uuid;
        this.info = new SimpleObjectProperty<>(info);
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

    public SavegameInfo getInfo() {
        return info.get();
    }

    public ObjectProperty<I> infoProperty() {
        return info;
    }
}
