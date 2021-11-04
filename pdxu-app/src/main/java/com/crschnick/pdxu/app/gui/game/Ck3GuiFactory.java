package com.crschnick.pdxu.app.gui.game;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.model.ck3.Ck3Person;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import com.crschnick.pdxu.model.ck3.Ck3Title;
import com.jfoenix.controls.JFXMasonryPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Ck3GuiFactory extends GameGuiFactory<Ck3Tag, Ck3SavegameInfo> {

    private static Region createRulerStatsNode(SavegameInfo<Ck3Tag> info, Ck3Person ruler) {
        var imgs = new Image[]{CK3_SKILL_DIPLOMACY, CK3_SKILL_MARTIAL, CK3_SKILL_STEWARDSHIP,
                CK3_SKILL_INTRIGUE, CK3_SKILL_LEARNING, CK3_SKILL_PROWESS};
        HBox skills = new HBox();
        skills.setAlignment(Pos.CENTER);
        for (int i = 0; i < 6; i++) {
            VBox box = new VBox();
            box.setAlignment(Pos.CENTER);
            box.getChildren().add(imageNode(imgs[i], "skill-icon"));
            int skill = i < ruler.getSkills().size() ? ruler.getSkills().get(i) : 0;
            box.getChildren().add(new Label("" + skill));
            skills.getChildren().add(box);
        }
        return skills;
    }

    @Override
    public Image tagImage(SavegameInfo<Ck3Tag> info, Ck3Tag tag) {
        if (tag == null) {
            return ImageHelper.DEFAULT_IMAGE;
        }

        return Ck3TagCache.realmImage(info, tag);
    }

    @Override
    public Pane background() {
        var bg = GameImage.backgroundNode(CK3_BACKGROUND);
        bg.setOpacity(0.4);
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
                t -> GameImage.imageNode(Ck3TagCache.titleImage(t, GameFileContext.fromInfo(info)), CLASS_TAG_ICON));

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
        GuiTooltips.install(stack, PdxuI18n.get("TREASURY_GOLD") + " / " + PdxuI18n.get("MONTHLY_INCOME"));
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
                t -> GameImage.imageNode(Ck3TagCache.realmImage(info, t), CLASS_TAG_ICON));

        row.getStyleClass().add(CLASS_DIPLOMACY_ROW);
        row.getStyleClass().add("realm-row");
        row.getStyleClass().add(style);
        addNode(pane, row);
    }
}
