package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisTag;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;

public class StellarisGuiFactory extends GameGuiFactory<StellarisTag, StellarisSavegameInfo> {

    @Override
    public Image tagImage(SavegameInfo<StellarisTag> info, StellarisTag tag) {
        return StellarisTagRenderer.createTagImage(info, tag);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(
                Files.newInputStream(GameInstallation.ALL.get(Game.STELLARIS)
                        .getInstallDir().resolve("launcher-assets").resolve("font.ttf")), 12);

    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(GameImage.STELLARIS_BACKGROUND);
        bg.setOpacity(0.3);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<StellarisTag> info) {
        return new Background(new BackgroundFill(
                Color.GRAY,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public void fillNodeContainer(SavegameInfo<StellarisTag> info, JFXMasonryPane grid) {
        super.fillNodeContainer(info, grid);
    }
}
