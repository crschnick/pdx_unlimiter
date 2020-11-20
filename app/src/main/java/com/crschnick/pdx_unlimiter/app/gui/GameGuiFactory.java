package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.eu4.Savegame;
import com.crschnick.pdx_unlimiter.eu4.SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

public abstract class GameGuiFactory<E extends GameCampaignEntry<? extends SavegameInfo>,C extends GameCampaign<E>> {

    public abstract Background createEntryInfoBackground(E entry);

    public abstract Pane createGameImage(C campaign);

    public abstract Pane createImage(E entry);
    public abstract ObservableValue<Pane> createImage(C campaign);

    public abstract String createInfoString(E entry);
    public abstract ObservableValue<String> createInfoString(C campaign);

    public abstract void fillNodeContainer(E entry, JFXMasonryPane grid);
}
