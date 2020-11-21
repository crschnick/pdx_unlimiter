package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.Hoi4Campaign;
import com.crschnick.pdx_unlimiter.app.game.Hoi4CampaignEntry;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_TAG_ICON;

public class Hoi4GuiFactory extends GameGuiFactory<Hoi4CampaignEntry, Hoi4Campaign> {


    private static javafx.scene.paint.Color colorFromInt(int c, int alpha) {
        return Color.rgb(c >>> 24, (c >>> 16) & 255, (c >>> 8) & 255, alpha / 255.0);
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.HOI4_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(Hoi4CampaignEntry entry) {
        return new Background(new BackgroundFill(
                colorFromInt(GameInstallation.HOI4.getCountryColors().get(entry.getTag().getTag()), 100),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public Pane createGameImage(Hoi4Campaign campaign) {
        return GameImage.hoi4TagNode(campaign.getTag(), CLASS_IMAGE_ICON);
    }

    @Override
    public Pane createImage(Hoi4CampaignEntry entry) {
        var icon = GameImage.hoi4TagNode(entry.getTag(), CLASS_TAG_ICON);
        Tooltip.install(icon, new Tooltip(GameInstallation.HOI4.getCountryNames().get(entry.getTag())));
        return icon;
    }

    @Override
    public ObservableValue<Pane> createImage(Hoi4Campaign campaign) {
        var b = Bindings.createObjectBinding(
                () -> GameImage.hoi4TagNode(campaign.getTag(), CLASS_TAG_ICON), campaign.tagProperty());
        return b;
    }

    @Override
    public String createInfoString(Hoi4CampaignEntry entry) {
        return entry.getDate().toString();
    }

    @Override
    public ObservableValue<String> createInfoString(Hoi4Campaign campaign) {
        var b = Bindings.createObjectBinding(
                () -> campaign.getDate().toString(), campaign.dateProperty());
        return b;
    }

    @Override
    public void fillNodeContainer(Hoi4CampaignEntry entry, JFXMasonryPane grid) {

    }
}
