package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.War;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Person;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3Title;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;
import static com.crschnick.pdx_unlimiter.app.gui.game.GameImage.*;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    public Ck3GuiFactory() {
        super("ck3", GameInstallation.CK3);
    }

    private static Region createRulerLabel(Ck3SavegameInfo info, Ck3Person ruler) {
        HBox rulerNode = new HBox();
        rulerNode.setSpacing(15);
        rulerNode.setAlignment(Pos.CENTER);
        {
            VBox box = new VBox();
            box.setSpacing(5);
            box.alignmentProperty().set(Pos.CENTER);

            {
                var topBar = new HBox();
                topBar.setSpacing(5);
                topBar.setAlignment(Pos.CENTER);
                topBar.getChildren().add(new HBox(GameImage.imageNode(CK3_ICON_RULER, "ruler-icon")));
                var name = new Label(info.getPlayerName() + " of " + info.getTag().getPrimaryTitle().getName());
                name.getStyleClass().add("ruler-name");
                topBar.getChildren().add(name);

                int age = GameDate.yearsBetween(ruler.getBirth(), info.getDate());
                var al = new Label(", " + age);
                al.getStyleClass().add("ruler-name");
                topBar.getChildren().add(al);

                box.getChildren().add(topBar);
            }

            box.getChildren().add(createRulerStatsNode(info, ruler));
            box.getStyleClass().add(CLASS_RULER);
            rulerNode.getChildren().add(box);
        }
        {
            var house = GameImage.imageNode(Ck3TagRenderer.houseImage(info, ruler.getHouse().getCoatOfArms()),
                    "house-icon");
            GuiTooltips.install(house, "House " + info.getHouseName());
            rulerNode.getChildren().add(house);
        }
        return rulerNode;
    }

    private static Region createRulerStatsNode(SavegameInfo<Ck3Tag> info, Ck3Person ruler) {
        var imgs = new Image[]{CK3_SKILL_DIPLOMACY, CK3_SKILL_MARTIAL, CK3_SKILL_STEWARDSHIP,
                CK3_SKILL_INTRIGUE, CK3_SKILL_LEARNING, CK3_SKILL_PROWESS};
        HBox skills = new HBox();
        skills.setAlignment(Pos.CENTER);
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
    protected Label createVersionInfo(SavegameInfo<Ck3Tag> info) {
        var l = super.createVersionInfo(info);
        if (SavegameActions.isVersionCompatible(info)) {
            l.setText(l.getText() + " " + GameInstallation.CK3.getVersion().getName());
        }
        return l;
    }

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        return Ck3TagRenderer.realmImage(info, tag.getPrimaryTitle().getCoatOfArms());
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

    private String getTitlesTooltip(List<Ck3Title> titles) {
        StringBuilder b = new StringBuilder();
        for (Ck3Title t : titles) {
            b.append(t.getName());
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private String getTagsTooltip(List<Ck3Tag> tags) {
        StringBuilder b = new StringBuilder();
        for (Ck3Tag t : tags) {
            b.append(t.getPrimaryTitle().getName());
            b.append(", ");
        }
        b.delete(b.length() - 2, b.length());
        return b.toString();
    }

    private void createDiplomacyRow(
            JFXMasonryPane pane,
            SavegameInfo<Ck3Tag> info,
            Node icon,
            List<Ck3Title> tags,
            String tooltipStart,
            String none,
            String style) {
        if (tags.size() == 0) {
            return;
        }

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(icon);
        int counter = 0;
        for (Ck3Title tag : tags) {
            Node n = GameImage.imageNode(Ck3TagRenderer.titleImage(info, tag.getCoatOfArms()), CLASS_TAG_ICON);
            box.getChildren().add(n);
            if (counter > 6) {
                box.getChildren().add(new Label("..."));
                break;
            }
            counter++;
        }
        box.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        box.getStyleClass().add(style);
        box.setSpacing(6);
        GuiTooltips.install(box, tooltipStart + (tags.size() > 0 ? getTitlesTooltip(tags) : none));
        addNode(pane, box);
    }

    private void createDiplomacyRealmRow(
            JFXMasonryPane pane,
            SavegameInfo<Ck3Tag> info,
            Node icon,
            List<Ck3Tag> tags,
            String tooltipStart,
            String none,
            String style) {
        if (tags.size() == 0) {
            return;
        }

        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(icon);
        int counter = 0;
        for (Ck3Tag tag : tags) {
            Node n = GameImage.imageNode(Ck3TagRenderer.realmImage(
                    info, tag.getPrimaryTitle().getCoatOfArms()), CLASS_TAG_ICON);
            box.getChildren().add(n);
            if (counter > 6) {
                box.getChildren().add(new Label("..."));
                break;
            }
            counter++;
        }
        box.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        box.getStyleClass().add(style);
        box.setSpacing(6);
        GuiTooltips.install(box, tooltipStart + (tags.size() > 0 ? getTagsTooltip(tags) : none));
        addNode(pane, box);
    }

    @Override
    public void fillNodeContainer(SavegameInfo<Ck3Tag> info, JFXMasonryPane grid) {
        addNode(grid, createRulerLabel((Ck3SavegameInfo) info, info.getTag().getRuler()));

        createDiplomacyRow(grid, info, GameImage.imageNode(CK3_ICON_TITLES, "tag-icon"),
                info.getTag().getTitles(), "Titles: ", "No titles", "title-row");
        createDiplomacyRow(grid, info, GameImage.imageNode(CK3_ICON_CLAIMS, "tag-icon"),
                info.getTag().getClaims(), "Claims: ", "No claims", "title-row");

        for (War<Ck3Tag> war : ((Ck3SavegameInfo) info).getWars()) {
            createDiplomacyRealmRow(grid, info, imageNode(CK3_ICON_WAR, CLASS_IMAGE_ICON),
                    war.isAttacker(info.getTag()) ? war.getDefenders() : war.getAttackers(),
                    "Fighting in the " + war.getTitle() + " against ", "", CLASS_WAR);
        }

        createDiplomacyRealmRow(grid, info, imageNode(CK3_ICON_ALLY, CLASS_IMAGE_ICON),
                ((Ck3SavegameInfo) info).getAllies(),
                "Allied with ", "", CLASS_ALLIANCE);

        if (info.isIronman()) {
            var ironman = new StackPane(imageNode(CK3_ICON_IRONMAN, CLASS_IMAGE_ICON, null));
            ironman.setAlignment(Pos.CENTER);
            GuiTooltips.install(ironman, "Ironman savegame");
            addNode(grid, ironman);
        }

        super.fillNodeContainer(info, grid);
    }
}
