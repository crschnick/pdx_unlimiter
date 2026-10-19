package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ColorHelper;
import com.crschnick.pdxu.model.stellaris.StellarisTag;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
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
        bg.opacityProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> {
                            return AppPrefs.get().theme().getValue().isDark() ? 0.05 : 0.13;
                        },
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
