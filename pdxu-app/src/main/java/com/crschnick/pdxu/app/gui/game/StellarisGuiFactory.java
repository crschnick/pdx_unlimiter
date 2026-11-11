package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;

public class StellarisGuiFactory extends GameGuiFactory<StellarisTag, StellarisSavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        return StellarisTagRenderer.createTagImage(GameFileContext.fromData(info.getData()), tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.STELLARIS_BACKGROUND);

        // Desaturate and darken / lighten the image instead of relying only on opacity,
        // so that the artwork keeps more of its contrast without fighting the UI colors
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
    public Background createEntryInfoBackground(SavegameInfo<StellarisTag> info) {
        var ctx = GameFileContext.fromData(info.getData());
        return StellarisTagRenderer.getMapColor(ctx, info.getData().getTag().getBackgroundPrimaryColor())
                .map(c -> new Background(new BackgroundFill(
                        ColorHelper.withAlpha(c, 0.33), new CornerRadii(4, 4, 0, 0, false), Insets.EMPTY)))
                .orElse(null);
    }
}
