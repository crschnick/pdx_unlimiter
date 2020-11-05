package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;

public abstract class GameIntegration<E extends GameCampaignEntry<? extends SavegameInfo>,C extends GameCampaign<E>> {

    public static Eu4Integration EU4;
    private static GameIntegration<GameCampaignEntry<SavegameInfo>, GameCampaign<GameCampaignEntry<SavegameInfo>>> current;
    private static SimpleObjectProperty<Optional<GameCampaign<GameCampaignEntry<SavegameInfo>>>> globalSelectedCampaign =
            new SimpleObjectProperty<>(Optional.empty());
    protected SimpleObjectProperty<Optional<GameCampaignEntry<SavegameInfo>>> globalSelectedEntry =
            new SimpleObjectProperty<>(Optional.empty());;

    public static void init() {
        EU4 = new Eu4Integration();
    }

    public static Optional<GameCampaign<GameCampaignEntry<SavegameInfo>>> getGlobalSelectedCampaign() {
        return globalSelectedCampaign.get();
    }

    public static SimpleObjectProperty<Optional<GameCampaign<GameCampaignEntry<SavegameInfo>>>>
    globalSelectedCampaignProperty() {
        return globalSelectedCampaign;
    }

    public static GameIntegration<GameCampaignEntry<SavegameInfo>, GameCampaign<GameCampaignEntry<SavegameInfo>>> current() {
        return current;
    }

    protected SimpleObjectProperty<Optional<C>> selectedCampaign = new SimpleObjectProperty<>(Optional.empty());
    protected SimpleObjectProperty<Optional<E>> selectedEntry = new SimpleObjectProperty<>(Optional.empty());;

    public abstract void launchCampaignEntry();

    public Optional<C> getSelectedCampaign() {
        return selectedCampaign.get();
    }

    public SimpleObjectProperty<Optional<C>> selectedCampaignProperty() {
        return selectedCampaign;
    }

    public Optional<E> getSelectedEntry() {
        return selectedEntry.get();
    }

    public SimpleObjectProperty<Optional<E>> selectedEntryProperty() {
        return selectedEntry;
    }

    public void setSelectedCampaign(Optional<C> selectedCampaign) {
        this.selectedCampaign.set(selectedCampaign);
    }

    public void setSelectedEntry(Optional<E> selectedEntry) {
        this.selectedEntry.set(selectedEntry);
    }
}
