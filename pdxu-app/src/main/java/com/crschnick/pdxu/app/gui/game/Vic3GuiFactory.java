package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.vic3.Vic3SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

import static com.crschnick.pdxu.app.gui.game.GameImage.VIC3_BACKGROUND;

public class Vic3GuiFactory extends GameGuiFactory<Vic3Tag, Vic3SavegameInfo> {


    @Override
    public Image tagImage(SavegameInfo<Vic3Tag> info, Vic3Tag tag) {
        if (tag == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        return Vic3CoatOfArmsCache.tagFlag(info, tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(VIC3_BACKGROUND);
        bg.opacityProperty().bind(Bindings.createDoubleBinding(() -> {
            return AppPrefs.get().theme().getValue().isDark() ? 0.05 : 0.16;
        }, AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Vic3Tag> info) {
        return null;
    }
}
