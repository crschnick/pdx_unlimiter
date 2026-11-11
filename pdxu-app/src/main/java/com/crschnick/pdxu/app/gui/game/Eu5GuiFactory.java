package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu5.Eu5SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.model.eu5.Eu5Tag;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Eu5GuiFactory extends GameGuiFactory<Eu5Tag, Eu5SavegameInfo> {

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.EU5_BACKGROUND);

        var adjust = new ColorAdjust();
        adjust.setSaturation(-1.0);
        adjust.setContrast(0.5);
        adjust.setBrightness(-0.5);
        bg.setEffect(adjust);

        bg.opacityProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> AppPrefs.get().theme().getValue().isDark() ? 0.1 : 0.2,
                        AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Eu5Tag> info) {
        var tagColor = info.getData().getTag() != null
                ? ColorHelper.fromGameColor(info.getData().eu5().getTag().getColor())
                : Color.BEIGE;
        Color color = ColorHelper.withAlpha(tagColor, 0.33);
        return new Background(new BackgroundFill(color, new CornerRadii(4, 4, 0, 0, false), Insets.EMPTY));
    }

    @Override
    public Image tagImage(SavegameInfo<Eu5Tag> info, Eu5Tag tag) {
        return Eu5CoatOfArmsCache.tagFlag(info.getData(), tag);
    }

    @Override
    public ObservableValue<String> createInfoString(SavegameCampaign<Eu5Tag, Eu5SavegameInfo> campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }
}
