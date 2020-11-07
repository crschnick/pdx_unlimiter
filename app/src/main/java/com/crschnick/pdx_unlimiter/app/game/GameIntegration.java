package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import javafx.beans.property.SimpleObjectProperty;

import javax.swing.event.ChangeListener;
import java.util.Optional;
import java.util.Set;

public abstract class GameIntegration<E extends GameCampaignEntry<? extends SavegameInfo>,C extends GameCampaign<E>> {

    public static Eu4Integration EU4;
    private static SimpleObjectProperty<GameIntegration<? extends GameCampaignEntry<? extends SavegameInfo>,
            ? extends GameCampaign<? extends GameCampaignEntry<? extends  SavegameInfo>>>> current = new SimpleObjectProperty<>();


    private static Set<GameIntegration<? extends GameCampaignEntry<? extends SavegameInfo>,
                ? extends GameCampaign<? extends GameCampaignEntry<? extends  SavegameInfo>>>> ALL;

    private static SimpleObjectProperty<? extends GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>>> globalSelectedCampaign =
            new SimpleObjectProperty<>();
    private static SimpleObjectProperty<? extends GameCampaignEntry<? extends SavegameInfo>> globalSelectedEntry =
            new SimpleObjectProperty<>();;

    public static void init() {
        EU4 = new Eu4Integration();
        ALL = Set.of(EU4);
        for (var i : ALL) {
            i.selectedEntryProperty().addListener((c,o,n) -> {
                globalSelectedEntryProperty().set(n);
            });
        }

        current.addListener((c,o,n) -> {
            globalSelectedCampaign = n.selectedCampaign;
            globalSelectedEntry = n.selectedEntry;
        });
        current.set(EU4);

    }

    public static GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>> getGlobalSelectedCampaign() {
        return globalSelectedCampaign.get();
    }

    public static SimpleObjectProperty<? extends GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>>> globalSelectedCampaignProperty() {
        return globalSelectedCampaign;
    }

    public static GameCampaignEntry<? extends SavegameInfo> getGlobalSelectedEntry() {
        return globalSelectedEntry.get();
    }

    public static <E extends GameCampaignEntry<? extends SavegameInfo>, C extends GameCampaign<E>>
    SimpleObjectProperty<E> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<E>) globalSelectedEntry;
    }

    public static <E extends GameCampaignEntry<? extends SavegameInfo>,
            C extends GameCampaign<E>> GameIntegration<E,C> current() {
        return (GameIntegration<E, C>) current.get();
    }

    protected SimpleObjectProperty<C> selectedCampaign = new SimpleObjectProperty<>();
    protected SimpleObjectProperty<E> selectedEntry = new SimpleObjectProperty<>();

    public abstract void launchCampaignEntry();

    public abstract boolean isVersionCompatibe(E entry);

    public abstract GameGuiFactory<E,C> getGuiFactory();

    public abstract SavegameCache<? extends SavegameInfo,E,C> getSavegameCache();

    public void openCampaignEntry(E entry) {

    }

    public C getSelectedCampaign() {
        return selectedCampaign.get();
    }

    public SimpleObjectProperty<C> selectedCampaignProperty() {
        return selectedCampaign;
    }

    public E getSelectedEntry() {
        return selectedEntry.get();
    }

    public SimpleObjectProperty<E> selectedEntryProperty() {
        return selectedEntry;
    }

    public void setSelectedCampaign(C selectedCampaign) {
        this.selectedCampaign.set(selectedCampaign);
    }

    public void setSelectedEntry(E selectedEntry) {
        this.selectedEntry.set(selectedEntry);
    }
}
