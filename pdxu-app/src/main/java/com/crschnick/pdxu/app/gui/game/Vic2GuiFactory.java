package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.vic2.Vic2SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.vic2.Vic2Tag;
import javafx.beans.binding.Bindings;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;

public class Vic2GuiFactory extends GameGuiFactory<Vic2Tag, Vic2SavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<Vic2Tag> info, Vic2Tag tag) {
        return ImageHelper.loadImage(GameInstallation.ALL.get(Game.VIC2).getInstallDir()
                .resolve("gfx").resolve("flags").resolve(tag.getTagId() + ".tga"));
    }

    @Override
    public Pane createIcon() {
        var icon = GameImage.getGameIcon(Game.VIC2);
        var cut = ImageHelper.cut(icon,
                new Rectangle2D(125, 0, Math.max(0, icon.getWidth() - 240), Math.max(0, icon.getHeight())));
        return GameImage.imageNode(cut, GuiStyle.CLASS_IMAGE_ICON);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.VIC2_BACKGROUND);
        bg.opacityProperty().bind(Bindings.createDoubleBinding(() -> {
            return AppPrefs.get().theme().getValue().isDark() ? 0.04 : 0.16;
        }, AppPrefs.get().theme()));
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Vic2Tag> info) {
        return null;
    }
}
