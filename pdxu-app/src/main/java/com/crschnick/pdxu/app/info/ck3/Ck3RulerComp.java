package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.gui.game.Ck3CoatOfArmsCache;
import com.crschnick.pdxu.app.gui.game.Ck3SkillModifierCache;
import com.crschnick.pdxu.app.gui.game.Ck3TraitCache;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.InfoNodes;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.app.util.ImageHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.ck3.Ck3Person;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Ck3RulerComp extends SavegameInfoComp {

    private static final int MAX_TRAITS = 8;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {}

    @Override
    public Region create(SavegameData<?> data) {
        var ruler = data.ck3().getTag().getRuler();

        var context = GameFileContext.fromData(data);
        var sources = new ArrayList<Ck3TraitCache.SkillSource>();
        sources.addAll(Ck3TraitCache.getTraitSkillSources(
                ruler.getTraits(), ruler.getTraitXp(), ruler.isFemale(), context));
        sources.addAll(
                Ck3SkillModifierCache.getModifierSkillSources(ruler.getModifiers(), ruler.getPerks(), context));
        var skills = new ArrayList<InfoNodes.ValueEntry>();
        var skillIcons = List.of(
                CK3_SKILL_DIPLOMACY,
                CK3_SKILL_MARTIAL,
                CK3_SKILL_STEWARDSHIP,
                CK3_SKILL_INTRIGUE,
                CK3_SKILL_LEARNING,
                CK3_SKILL_PROWESS);
        for (int i = 0; i < skillIcons.size(); i++) {
            int base = i < ruler.getSkills().size() ? ruler.getSkills().get(i) : 0;
            var skillName = GameLocalisation.getLocalisedValue(Ck3TraitCache.SKILLS.get(i), data);
            var tooltip = new StringBuilder(skillName + ": " + AppI18n.get("baseValue", String.valueOf(base)));
            int total = base;
            for (var source : sources) {
                var value = source.skills()[i];
                if (value == 0) {
                    continue;
                }

                total += value;
                tooltip.append("\n")
                        .append(value > 0 ? "+" : "")
                        .append(value)
                        .append(" ")
                        .append(GameLocalisation.getLocalisedValue(source.nameKey(), data));
            }
            skills.add(new InfoNodes.ValueEntry(
                    String.valueOf(Math.max(0, total)), skillIcons.get(i), tooltip.toString()));
        }

        var name = data.ck3().getTag().getName();
        var age = GameDate.yearsBetween(ruler.getBirth(), data.ck3().getDate());
        var stats = new VBox(InfoNodes.createValueRow(skills), createTraitsNode(data, ruler));
        stats.setSpacing(5);
        stats.setAlignment(Pos.CENTER);
        var rulerNode = InfoNodes.createRulerNode(CK3_ICON_RULER, name + ", " + age, null, stats);
        if (ruler.getHouse() == null) {
            return rulerNode;
        }

        var house = GameImage.imageNode(
                Ck3CoatOfArmsCache.houseImage(ruler.getHouse(), context), "house-icon");
        GuiTooltips.install(house, AppI18n.get("house", ruler.getHouse().getName()));
        var box = new HBox(rulerNode, house);
        box.setSpacing(15);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private static Region createTraitsNode(SavegameData<?> data, Ck3Person ruler) {
        var context = GameFileContext.fromData(data);
        var box = new HBox();
        box.setSpacing(2);
        box.setAlignment(Pos.CENTER);
        var overflow = new ArrayList<String>();
        for (var trait : ruler.getTraits().reversed()) {
            var image = Ck3TraitCache.traitImage(trait, context);
            if (image == ImageHelper.DEFAULT_IMAGE) {
                continue;
            }

            var name = GameLocalisation.getLocalisedValue(Ck3TraitCache.traitNameKey(trait, ruler.isFemale(), context), data);
            if (box.getChildren().size() >= MAX_TRAITS) {
                overflow.add(name);
                continue;
            }

            var icon = GameImage.imageNode(image, "trait-icon");
            GuiTooltips.install(icon, name);
            box.getChildren().add(icon);
        }

        if (!overflow.isEmpty()) {
            var more = new Label("...");
            GuiTooltips.install(more, String.join("\n", overflow));
            box.getChildren().add(more);
        }
        return box;
    }
}
