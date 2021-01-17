package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.core.data.GameDate;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.scene.image.Image;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GameCampaign<T, I extends SavegameInfo<T>> extends SavegameCollection<T, I> {

    private volatile ObjectProperty<GameDate> date;
    private ObjectProperty<Image> image;

    public GameCampaign(Instant lastPlayed, String name, UUID campaignId, GameDate date, Image image) {
        super(lastPlayed, name, campaignId);
        this.date = new SimpleObjectProperty<>(date);
        this.image = new SimpleObjectProperty<>(image);

        getSavegames().addListener((SetChangeListener<? super GameCampaignEntry<T, I>>) (change) -> {
            boolean isNewEntry = change.wasAdded() && change.getElementAdded().infoProperty().isNotNull().get();
            boolean wasRemoved = change.wasRemoved();
            if (isNewEntry || wasRemoved) {
                getSavegames().stream()
                        .filter(s -> s.infoProperty().isNotNull().get())
                        .min(Comparator.naturalOrder())
                        .map(s -> s.getInfo().getDate())
                        .ifPresent(d -> dateProperty().setValue(d));

                getSavegames().stream()
                        .filter(s -> s.infoProperty().isNotNull().get())
                        .min(Comparator.naturalOrder())
                        .ifPresent(e -> imageProperty().set(SavegameActions.createImageForEntry(e)));
            }
        });
    }

    public Image getImage() {
        return image.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public GameCampaignEntry<T, I> getLatestEntry() {
        return entryStream().findFirst().get();
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

    public GameDate getDate() {
        return date.get();
    }

    public ObjectProperty<GameDate> dateProperty() {
        return date;
    }
}
