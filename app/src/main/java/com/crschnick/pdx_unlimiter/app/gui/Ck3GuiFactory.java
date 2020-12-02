package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.eu4.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.eu4.savegame.Ck3SavegameInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_TAG_ICON;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    public Ck3GuiFactory() {
        super(GameInstallation.CK3);
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.CK3_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        return new Background(new BackgroundFill(
                Color.ALICEBLUE,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public ObservableValue<Pane> createImage(GameCampaignEntry<Ck3Tag, Ck3SavegameInfo> entry) {
        SimpleObjectProperty<Pane> prop = new SimpleObjectProperty<>(GameImage.ck3TagNode(entry.getTag(), CLASS_TAG_ICON));
        entry.infoProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                //prop.set(GameImage.eu4TagNode(entry, CLASS_TAG_ICON));
            });
        });
        return prop;
    }

    @Override
    public ObservableValue<Pane> createImage(GameCampaign<Ck3Tag, Ck3SavegameInfo> campaign) {
        SimpleObjectProperty<Pane> prop = new SimpleObjectProperty<>(GameImage.ck3TagNode(campaign.getTag(), CLASS_TAG_ICON));
        campaign.tagProperty().addListener((c, o, n) -> {
            //Platform.runLater(() -> prop.set(GameImage.hoi4TagNode(campaign.getTag(), CLASS_TAG_ICON)));
        });
        return prop;
    }
}
