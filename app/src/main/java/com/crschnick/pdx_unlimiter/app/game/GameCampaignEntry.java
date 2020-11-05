package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import java.util.Optional;
import java.util.UUID;

public class GameCampaignEntry<I extends SavegameInfo> {
    private StringProperty name;
    private UUID uuid;
    private ObjectProperty<Optional<I>> info;

    public GameCampaignEntry(StringProperty name, UUID uuid, ObjectProperty<Optional<I>> info) {
        this.name = name;
        this.uuid = uuid;
        this.info = info;
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

    public Optional<I> getInfo() {
        return info.get();
    }

    public ObjectProperty<Optional<I>> infoProperty() {
        return info;
    }
}
