package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu5.Eu5SavegameInfo;
import com.crschnick.pdxu.app.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.GameColor;
import com.crschnick.pdxu.model.eu5.Eu5Tag;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.nio.file.Path;

public class Eu5GuiFactory extends GameGuiFactory<Eu5Tag, Eu5SavegameInfo> {

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.EU5_BACKGROUND);
        bg.opacityProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> {
                            return AppPrefs.get().theme().getValue().isDark() ? 0.07 : 0.17;
                        },
                        AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Eu5Tag> info) {
        var tagColor = info.getData().getTag() != null ? ColorHelper.fromGameColor(info.getData().eu5().getTag().getColor()) : Color.BEIGE;
        Color color = ColorHelper.withAlpha(tagColor, 0.33);
        return new Background(new BackgroundFill(
                color, new CornerRadii(4, 4, 0, 0, false), Insets.EMPTY));
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
