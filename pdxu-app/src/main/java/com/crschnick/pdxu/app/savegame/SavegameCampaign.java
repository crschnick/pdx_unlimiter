package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.model.GameDate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public final class SavegameCampaign<T, I extends SavegameInfo<T>> {

    private final ObjectProperty<GameDate> date;
    private final ObjectProperty<Image> image;
    private final ObjectProperty<Instant> lastPlayed;
    private final StringProperty name;
    private final UUID uuid;
    private final ObservableSet<SavegameEntry<T, I>> savegames = FXCollections.observableSet(new HashSet<>());

    public SavegameCampaign(Instant lastPlayed, String name, UUID campaignId, GameDate date, Image image) {
        this.lastPlayed = new SimpleObjectProperty<>(lastPlayed);
        this.name = new SimpleStringProperty(name);
        this.uuid = campaignId;
        this.date = new SimpleObjectProperty<>(date);
        this.image = new SimpleObjectProperty<>(image);
    }

    public void onSavegameLoad(SavegameEntry<T, I> entry) {
        if (entry == getLatestEntry()) {
            imageProperty().set(SavegameActions.createImageForEntry(entry));
            updateDate();
        }
    }

    public void onSavegamesChange() {
        updateDate();
    }

    private void updateDate() {
        getSavegames().stream()
                .filter(s -> s.infoProperty().isNotNull().get())
                .min(Comparator.naturalOrder())
                .map(s -> s.getInfo().getData().getDate())
                .ifPresent(d -> dateProperty().setValue(d));
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public SavegameEntry<T, I> getLatestEntry() {
        return entryStream().findFirst().get();
    }

    public Stream<SavegameEntry<T, I>> entryStream() {
        var list = new ArrayList<>(getSavegames());
        list.sort(Comparator.comparing(SavegameEntry::getDate));
        Collections.reverse(list);
        return list.stream();
    }

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
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

    public Instant getLastPlayed() {
        return lastPlayed.get();
    }

    public ObjectProperty<Instant> lastPlayedProperty() {
        return lastPlayed;
    }
}
