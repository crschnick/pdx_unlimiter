package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_BACKGROUND;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        if (tag == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        return Ck3CoatOfArmsCache.realmImage(info.getData(), tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK3_BACKGROUND);
        bg.opacityProperty().bind(Bindings.createDoubleBinding(() -> {
            return AppPrefs.get().theme().getValue().isDark() ? 0.05 : 0.15;
        }, AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Ck3Tag> info) {
        return new Background(new BackgroundFill(
                Ck3Backgrounds.getBackgroundColor(info),
                new CornerRadii(4, 4, 0, 0, false), Insets.EMPTY));
    }
}
