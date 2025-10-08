package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.util.CascadeDirectoryHelper;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

import java.nio.file.Path;

public class Hoi4GuiFactory extends GameGuiFactory<Hoi4Tag, Hoi4SavegameInfo> {

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.HOI4_BACKGROUND);
        bg.opacityProperty().bind(Bindings.createDoubleBinding(() -> {
            return AppPrefs.get().theme().getValue().isDark() ? 0.05 : 0.13;
        }, AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Hoi4Tag> info) {
        return null;
    }

    @Override
    public Image tagImage(SavegameInfo<Hoi4Tag> info, Hoi4Tag tag) {
        return hoi4TagNode(GameImage.getHoi4TagPath(tag), info);
    }

    private Image hoi4TagNode(Path path, SavegameInfo<Hoi4Tag> info) {
        var in = CascadeDirectoryHelper.openFile(path, info);
        return ImageHelper.loadImage(in.orElse(null), null);
    }

    @Override
    public ObservableValue<String> createInfoString(SavegameCampaign<Hoi4Tag, Hoi4SavegameInfo> campaign) {
        SimpleStringProperty prop = new SimpleStringProperty(campaign.getDate().toString());
        campaign.dateProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> prop.set(n.toString()));
        });
        return prop;
    }
}
