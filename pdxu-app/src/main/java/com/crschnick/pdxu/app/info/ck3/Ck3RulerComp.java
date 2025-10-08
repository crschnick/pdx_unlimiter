package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.Ck3CoatOfArmsCache;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.installation.GameFileContext;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.GameDate;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.NoArgsConstructor;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_RULER;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

@NoArgsConstructor
public class Ck3RulerComp extends SavegameInfoComp {

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {

    }

    @Override
    public Region create(SavegameData<?> data) {
        var ruler = data.ck3().getTag().getRuler();

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
                int age = GameDate.yearsBetween(ruler.getBirth(), data.ck3().getDate());
                var title = new Label(data.ck3().getTag().getName() + ", " + age);
                title.getStyleClass().add("ruler-name");
                topBar.getChildren().add(title);

                box.getChildren().add(topBar);
            }

            box.getChildren().add(skills);
            box.getStyleClass().add(CLASS_RULER);
            rulerNode.getChildren().add(box);
        }
        {
            var house = GameImage.imageNode(
                    Ck3CoatOfArmsCache.houseImage(ruler.getHouse(), GameFileContext.fromData(data)),
                    "house-icon");
            GuiTooltips.install(house, AppI18n.get("house", ruler.getHouse().getName()));
            rulerNode.getChildren().add(house);
        }
        return rulerNode;
    }
}
