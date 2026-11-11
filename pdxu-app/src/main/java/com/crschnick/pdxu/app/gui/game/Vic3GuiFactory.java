package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.vic3.Vic3SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

import javafx.beans.binding.Bindings;
import javafx.scene.effect.ColorAdjust;
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
        var bg = GameImage.backgroundNode(GameImage.VIC3_BACKGROUND);

        var adjust = new ColorAdjust();
        adjust.setSaturation(-1.0);
        adjust.setContrast(0.5);
        adjust.setBrightness(-0.5);
        bg.setEffect(adjust);

        bg.opacityProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> AppPrefs.get().theme().getValue().isDark() ? 0.08 : 0.2,
                        AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Vic3Tag> info) {
        return null;
    }
}
