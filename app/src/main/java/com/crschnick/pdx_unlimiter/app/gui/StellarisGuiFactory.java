package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.eu4.data.StellarisTag;
import com.crschnick.pdx_unlimiter.eu4.savegame.StellarisSavegameInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_IMAGE_ICON;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_TAG_ICON;

public class StellarisGuiFactory extends GameGuiFactory<StellarisTag, StellarisSavegameInfo> {

    public StellarisGuiFactory() {
        super(GameInstallation.STELLARIS);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.STELLARIS.getPath().resolve("launcher-assets").resolve("font.ttf")), 12);

    }

    @Override
    public Pane background() {
        return GameImage.backgroundNode(GameImage.STELLARIS_BACKGROUND);
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.STELLARIS_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry) {
        return new Background(new BackgroundFill(
                Color.GRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public ObservableValue<Pane> createImage(GameCampaignEntry<StellarisTag, StellarisSavegameInfo> entry) {
        SimpleObjectProperty<Pane> prop = new SimpleObjectProperty<>(GameImage.stellarisTagNode(entry, CLASS_TAG_ICON));
        entry.infoProperty().addListener((c, o, n) -> {
            prop.set(GameImage.stellarisTagNode(entry, CLASS_TAG_ICON));
            Tooltip.install(prop.get(), new Tooltip());
        });
        return prop;
    }

    @Override
    public ObservableValue<Pane> createImage(GameCampaign<StellarisTag, StellarisSavegameInfo> campaign) {
        SimpleObjectProperty<Pane> prop = new SimpleObjectProperty<>(GameImage.stellarisTagNode(campaign.getTag(), CLASS_TAG_ICON));
        prop.bind(createImage(campaign.getLatestEntry()));
        campaign.getEntries().addListener((SetChangeListener<? super GameCampaignEntry<StellarisTag, StellarisSavegameInfo> >) c -> {
            prop.bind(createImage(campaign.getLatestEntry()));
        });
        return prop;
    }
}
