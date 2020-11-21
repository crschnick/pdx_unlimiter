package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class GameIntegration<E extends GameCampaignEntry<? extends SavegameInfo>,C extends GameCampaign<E>> {

    private static SimpleObjectProperty<GameIntegration<? extends GameCampaignEntry<? extends SavegameInfo>,
            ? extends GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>>>> current = new SimpleObjectProperty<>();


    private static List<GameIntegration<? extends GameCampaignEntry<? extends SavegameInfo>,
                ? extends GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>>>> ALL;

    private static SimpleObjectProperty<? extends GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>>> globalSelectedCampaign =
            new SimpleObjectProperty<>();
    private static SimpleObjectProperty<? extends GameCampaignEntry<? extends SavegameInfo>> globalSelectedEntry =
            new SimpleObjectProperty<>();

    public static void init() {
        ALL = new ArrayList<>();
        if (Settings.getInstance().getEu4().isPresent()) {
            ALL.add(new Eu4Integration());
        }
        if (Settings.getInstance().getHoi4().isPresent()) {
            ALL.add(new Hoi4Integration());
        }
        current.set(ALL.get(1));
    }

    public static List<GameIntegration<?,?>> getAvailable() {
        return (List<GameIntegration<?, ?>>) ALL;
    }

    public static <E extends GameCampaignEntry<? extends SavegameInfo>,
            C extends GameCampaign<E>> ReadOnlyObjectProperty<C> globalSelectedCampaignProperty() {
        return (SimpleObjectProperty<C>) globalSelectedCampaign;
    }

    private static <E extends GameCampaignEntry<? extends SavegameInfo>,
            C extends GameCampaign<E>> SimpleObjectProperty<C> globalSelectedCampaignPropertyInternal() {
        return (SimpleObjectProperty<C>) globalSelectedCampaign;
    }



    public static <E extends GameCampaignEntry<? extends SavegameInfo>, C extends GameCampaign<E>>
    ReadOnlyObjectProperty<E> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<E>) globalSelectedEntry;
    }

    private static <E extends GameCampaignEntry<? extends SavegameInfo>, C extends GameCampaign<E>>
    SimpleObjectProperty<E> globalSelectedEntryPropertyInternal() {
        return (SimpleObjectProperty<E>) globalSelectedEntry;
    }

    public static <E extends GameCampaignEntry<? extends SavegameInfo>,
            C extends GameCampaign<E>> GameIntegration<E,C> current() {
        return (GameIntegration<E, C>) current.get();
    }

    public static SimpleObjectProperty<GameIntegration<? extends GameCampaignEntry<? extends SavegameInfo>,
            ? extends GameCampaign<? extends GameCampaignEntry<? extends SavegameInfo>>>> currentGameProperty() {
        return current;
    }

    protected SimpleObjectProperty<C> selectedCampaign = new SimpleObjectProperty<>();
    protected SimpleObjectProperty<E> selectedEntry = new SimpleObjectProperty<>();

    public abstract String getName();

    public abstract void launchCampaignEntry();

    public abstract boolean isVersionCompatibe(E entry);

    public abstract GameGuiFactory<E,C> getGuiFactory();

    public abstract SavegameCache<? extends SavegameInfo,E,C> getSavegameCache();

    public void openCampaignEntry(E entry) {
        try {
            Desktop.getDesktop().open(getSavegameCache().getPath(entry).toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectCampaign(C c) {
        this.selectedEntry.set(null);
        globalSelectedEntryPropertyInternal().set(null);
        this.selectedCampaign.set(c);
        globalSelectedCampaignPropertyInternal().set((GameCampaign<GameCampaignEntry<? extends SavegameInfo>>) c);
        LoggerFactory.getLogger(GameIntegration.class).debug("Selecting campaign " + (c != null ? c.getName() : "null"));
    }

    public void selectEntry(E e) {
        if (e != null) {
            this.selectedCampaign.set(getSavegameCache().getCampaign(e));
            globalSelectedCampaignPropertyInternal().set((GameCampaign<GameCampaignEntry<? extends SavegameInfo>>) getSavegameCache().getCampaign(e));
        }
        this.selectedEntry.set(e);
        globalSelectedEntryPropertyInternal().set(e);

        LoggerFactory.getLogger(GameIntegration.class).debug("Selecting campaign entry " + (e != null ? e.getName() : "null"));
    }

    public static void selectIntegration(GameIntegration<?,?> newInt) {
        current().selectCampaign(null);
        current.set(newInt);
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
}
