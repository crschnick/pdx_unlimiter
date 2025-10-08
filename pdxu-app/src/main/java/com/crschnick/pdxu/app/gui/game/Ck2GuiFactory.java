package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.ck2.Ck2SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.ck2.Ck2Tag;
import javafx.beans.binding.Bindings;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

public class Ck2GuiFactory extends GameGuiFactory<Ck2Tag, Ck2SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Ck2Tag> info, Ck2Tag tag) {
        return ImageHelper.loadImage(GameInstallation.ALL.get(Game.CK2).getInstallDir()
                .resolve("gfx").resolve("flags").resolve(tag.getPrimaryTitle() + ".tga"));
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.CK2_BACKGROUND);
        bg.opacityProperty().bind(Bindings.createDoubleBinding(() -> {
            return AppPrefs.get().theme().getValue().isDark() ? 0.05 : 0.17;
        }, AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Ck2Tag> info) {
        return null;
    }
}
