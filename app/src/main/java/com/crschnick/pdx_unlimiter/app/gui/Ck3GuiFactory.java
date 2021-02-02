package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;

import static com.crschnick.pdx_unlimiter.app.gui.GameImage.*;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    public Ck3GuiFactory() {
        super(GameInstallation.CK3);
    }

    @Override
    protected Label createVersionInfo(SavegameInfo<Ck3Tag> info) {
        var l = super.createVersionInfo(info);
        if (SavegameActions.isVersionCompatible(info)) {
            l.setText(l.getText() + " " + GameInstallation.CK3.getVersion().getName());
        }
        return l;
    }

    private static Region createRulerLabel(SavegameInfo<Ck3Tag> info, Ck3Tag.Person ruler) {
        HBox rulerNode = new HBox();
        {
            VBox box = new VBox();
            box.alignmentProperty().set(Pos.CENTER);
            box.getChildren().add(new Label(ruler.getFirstName() + " " + ruler.getHouse().get().getName()));
            box.getChildren().add(createRulerStatsNode(info, ruler));
            box.getStyleClass().add(CLASS_RULER);
            rulerNode.getChildren().add(box);
        }
        {
            var h = ruler.getHouse().get();
            var house = GameImage.imageNode(Ck3TagRenderer.tagImage(info, h.getCoatOfArms().get(), Ck3TagRenderer.Type.HOUSE), CLASS_TAG_ICON);
            rulerNode.getChildren().add(house);
        }
        return rulerNode;
    }

    private static Region createRulerStatsNode(SavegameInfo<Ck3Tag> info, Ck3Tag.Person ruler) {
        var imgs = new Image[] {CK3_SKILL_DIPLOMACY, CK3_SKILL_MARTIAL, CK3_SKILL_STEWARDSHIP,
                CK3_SKILL_INTRIGUE, CK3_SKILL_LEARNING, CK3_SKILL_PROWESS};
        HBox skills = new HBox();
        for (int i = 0; i < 6; i++) {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            box.getChildren().add(imageNode(imgs[i], "skill-icon"));
            box.getChildren().add(new Label("" + ruler.getSkills().get(i)));
            skills.getChildren().add(box);
        }
        return skills;
    }

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        return Ck3TagRenderer.tagImage(info, tag.getPrimaryTitle().getCoatOfArms(), Ck3TagRenderer.Type.HOUSE);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(Files.newInputStream(GameInstallation.CK3.getPath()
                .resolve("launcher").resolve("assets").resolve("fonts").resolve("CormorantGaramond-Regular.ttf")), 12);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK3_BACKGROUND);
        bg.setOpacity(0.3);
        return bg;
    }

    @Override
    public Pane createIcon() {
        return GameImage.imageNode(GameImage.CK3_ICON, CLASS_IMAGE_ICON);
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Ck3Tag> info) {
        return new Background(new BackgroundFill(
                Color.CORAL,
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    @Override
    public void fillNodeContainer(SavegameInfo<Ck3Tag> info, JFXMasonryPane grid) {
        addNode(grid,createRulerLabel(info, info.getTag().getRuler()) );
        super.fillNodeContainer(info, grid);
    }
}
