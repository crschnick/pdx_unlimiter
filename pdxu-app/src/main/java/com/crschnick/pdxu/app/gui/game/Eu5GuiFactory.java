package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu5.Eu5SavegameInfo;
import com.crschnick.pdxu.app.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.eu5.Eu5Tag;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

import java.nio.file.Path;

public class Eu5GuiFactory extends GameGuiFactory<Eu5Tag, Eu5SavegameInfo> {

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.EU5_BACKGROUND);
        bg.opacityProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> {
                            return AppPrefs.get().theme().getValue().isDark() ? 0.05 : 0.06;
                        },
                        AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Eu5Tag> info) {
        return null;
    }

    @Override
    public Image tagImage(SavegameInfo<Eu5Tag> info, Eu5Tag tag) {
        if (tag == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        return Eu5CoatOfArmsCache.tagFlag(info, tag);
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
