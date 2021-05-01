package com.crschnick.pdx_unlimiter.app.gui.game;

import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.installation.Game;
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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;
import static com.crschnick.pdx_unlimiter.app.gui.game.GameImage.*;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    private static Region createRulerLabel(Ck3SavegameInfo info, Ck3Person ruler) {
        HBox rulerNode = new HBox();
        rulerNode.setSpacing(15);
        rulerNode.setAlignment(Pos.CENTER);
        rulerNode.getStyleClass().add("ruler-info");
        {
            VBox box = new VBox();
            box.setSpacing(5);
            box.alignmentProperty().set(Pos.CENTER);

            {
                var topBar = new HBox();
                topBar.setSpacing(5);
                topBar.setAlignment(Pos.CENTER);
                topBar.getChildren().add(new HBox(GameImage.imageNode(CK3_ICON_RULER, "ruler-icon")));
                int age = GameDate.yearsBetween(ruler.getBirth(), info.getDate());
                var title = new Label(info.getPlayerName() + ", " + age);
                title.getStyleClass().add("ruler-name");
                topBar.getChildren().add(title);

                box.getChildren().add(topBar);
            }

            box.getChildren().add(createRulerStatsNode(info, ruler));
            box.getStyleClass().add(CLASS_RULER);
            rulerNode.getChildren().add(box);
        }
        {
            var house = GameImage.imageNode(Ck3TagRenderer.houseImage(info, ruler.getHouse()),
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
            l.setText(l.getText() + " " + GameInstallation.ALL.get(Game.CK3).getVersion().getName());
        }
        return l;
    }

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        if (tag == null) {
            return ImageLoader.DEFAULT_IMAGE;
        }

        return Ck3TagRenderer.realmImage(info, tag);
    }

    @Override
    public Font font() throws IOException {
        return Font.loadFont(Files.newInputStream(GameInstallation.ALL.get(Game.CK3).getInstallDir()
                .resolve("launcher").resolve("assets").resolve("fonts").resolve("CormorantGaramond-Regular.ttf")), 12);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK3_BACKGROUND);
        bg.setOpacity(0.3);
        return bg;
    }

    @Override
    public Background createEntryInfoBackground(SavegameInfo<Ck3Tag> info) {
        return new Background(new BackgroundFill(
                Ck3Backgrounds.getBackgroundColor(info),
                CornerRadii.EMPTY, Insets.EMPTY));
    }

    private void createTitleRow(
            JFXMasonryPane pane,
            SavegameInfo<Ck3Tag> info,
            Region icon,
            List<Ck3Title> titles,
            String tooltip,
            String style) {
        if (titles.size() == 0) {
            return;
        }

        var row = TagRows.createTagRow(
                icon,
                tooltip,
                titles.stream().filter(t -> !t.getType().equals(Ck3Title.Type.BARONY)).collect(Collectors.toList()),
                t -> t.getName(),
                t -> GameImage.imageNode(Ck3TagRenderer.titleImage(info, t), CLASS_TAG_ICON));

        row.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        row.getStyleClass().add("title-row");
        row.getStyleClass().add(style);
        addNode(pane, row);
    }

    private void addGoldEntry(
            JFXMasonryPane pane,
            int value,
            int income) {
        var valueDisplay = new Label(" " + value + " / " + (income > 0 ? "+" : "") + income);
        valueDisplay.setMinWidth(Region.USE_PREF_SIZE);
        valueDisplay.setEllipsisString("");

        HBox hbox = new HBox(GameImage.imageNode(CK3_ICON_GOLD, CLASS_IMAGE_ICON), valueDisplay);
        hbox.setAlignment(Pos.CENTER);
        hbox.setMinWidth(Region.USE_PREF_SIZE);

        var stack = new StackPane(hbox);
        stack.setAlignment(Pos.CENTER);
        stack.setMinWidth(Region.USE_PREF_SIZE);
        stack.getStyleClass().add("number");
        GuiTooltips.install(stack, "Gold in treasury / Monthly income");
        addNode(pane, stack);
    }

    private void createRealmRow(
            JFXMasonryPane pane,
            SavegameInfo<Ck3Tag> info,
            Region icon,
            List<Ck3Tag> tags,
            String tooltip,
            String style) {
        if (tags.size() == 0) {
            return;
        }

        var row = TagRows.createTagRow(
                icon,
                tooltip,
                tags,
                t -> t.getName(),
                t -> GameImage.imageNode(Ck3TagRenderer.realmImage(info, t), CLASS_TAG_ICON));

        row.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        row.getStyleClass().add("realm-row");
        row.getStyleClass().add(style);
        addNode(pane, row);
    }

    @Override
    public void fillNodeContainer(SavegameInfo<Ck3Tag> info, JFXMasonryPane grid) {
        if (info.hasOnePlayerTag()) {
            addNode(grid, createRulerLabel((Ck3SavegameInfo) info, info.getTag().getRuler()));
        }

        if (info.isIronman()) {
            var ironman = new StackPane(imageNode(CK3_ICON_IRONMAN, CLASS_IMAGE_ICON, null));
            ironman.setAlignment(Pos.CENTER);
            GuiTooltips.install(ironman, "Ironman savegame");
            addNode(grid, ironman);
        }

        if (info.hasOnePlayerTag()) {
            addGoldEntry(grid, info.getTag().getGold(), info.getTag().getIncome());
            addIntegerEntry(grid, CK3_ICON_PRESTIGE, info.getTag().getPrestige(), "Prestige", false);
            addIntegerEntry(grid, CK3_ICON_PIETY, info.getTag().getPiety(), "Piety", false);
            addIntegerEntry(grid, CK3_ICON_SOLDIERS, info.getTag().getStrength(), "Total soldiers", false);

            createTitleRow(grid, info, GameImage.imageNode(CK3_ICON_TITLES, "tag-icon"),
                    info.getTag().getTitles(), "Titles", "titles");
            createTitleRow(grid, info, GameImage.imageNode(CK3_ICON_CLAIMS, "tag-icon"),
                    info.getTag().getClaims(), "Claims", "claims");

            for (War<Ck3Tag> war : ((Ck3SavegameInfo) info).getWars()) {
                createRealmRow(grid, info, imageNode(CK3_ICON_WAR, CLASS_IMAGE_ICON),
                        war.isAttacker(info.getTag()) ? war.getDefenders() : war.getAttackers(),
                        war.getTitle(), CLASS_WAR);
            }

            createRealmRow(grid, info, imageNode(CK3_ICON_ALLY, CLASS_IMAGE_ICON),
                    ((Ck3SavegameInfo) info).getAllies(),
                    "Allies", CLASS_ALLIANCE);
        }

        super.fillNodeContainer(info, grid);
    }
}
