package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.gui.game.Hoi4GuiFactory;
import com.crschnick.pdxu.app.info.DiplomacyRowComp;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_TAG_ICON;

public class Hoi4WarsComp extends DiplomacyRowComp<Hoi4WarsComp.Enemy> {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Enemy {
        private String tag;
        private String ideology;
        private String cosmeticTag;
        private long ownCasualties;
        private long enemyCasualties;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        tags = new ArrayList<>();
        var playerTag = data.hoi4().getTag().getTag();
        var countries = content.get().getNodeForKey("countries");
        countries
                .getNodeForKey(playerTag)
                .getNodeForKeyIfExistent("diplomacy")
                .flatMap(d -> d.getNodeForKeyIfExistent("active_relations"))
                .ifPresent(relations -> relations.forEach((otherTag, relation) -> {
                    if (otherTag == null || !relation.isArray()) {
                        return;
                    }

                    relation.getNodeForKeyIfExistent("war_relation").ifPresent(war -> {
                        var playerIsFirst = war.getNodeForKeyIfExistent("first")
                                .map(Node::getString)
                                .map(playerTag::equals)
                                .orElse(true);
                        var firstCasualties = getLong(war, "first_casualties");
                        var secondCasualties = getLong(war, "second_casualties");

                        var other = countries.getNodeForKeyIfExistent(otherTag);
                        var ideology = other.flatMap(o -> o.getNodeForKeyIfExistent("politics"))
                                .flatMap(p -> p.getNodeForKeyIfExistent("ruling_party"))
                                .map(Node::getString)
                                .orElse(null);
                        var cosmeticTag = other.flatMap(o -> o.getNodeForKeyIfExistent("cosmetic_tag"))
                                .map(Node::getString)
                                .filter(s -> !s.isBlank())
                                .orElse(null);
                        tags.add(new Enemy(
                                otherTag,
                                ideology,
                                cosmeticTag,
                                playerIsFirst ? firstCasualties : secondCasualties,
                                playerIsFirst ? secondCasualties : firstCasualties));
                    });
                }));
    }

    private static long getLong(Node war, String key) {
        return war.getNodeForKeyIfExistent(key)
                .filter(Node::isValue)
                .map(n -> (long) n.getDouble())
                .orElse(0L);
    }

    @Override
    protected Region mapTag(SavegameData<?> data, Enemy enemy) {
        return GameImage.imageNode(
                Hoi4GuiFactory.countryFlag(data, enemy.tag, enemy.ideology, enemy.cosmeticTag), CLASS_TAG_ICON);
    }

    @Override
    protected String mapTagTooltip(SavegameData<?> data, Enemy enemy) {
        return Hoi4Names.getCountryName(data, enemy.tag, enemy.ideology, enemy.cosmeticTag) + "\n"
                + AppI18n.get("warCasualties") + ": " + NumberFormatHelper.formatCompact(enemy.ownCasualties) + " / "
                + NumberFormatHelper.formatCompact(enemy.enemyCasualties);
    }

    @Override
    protected String getStyleClass() {
        return GuiStyle.CLASS_WAR;
    }

    @Override
    protected String getIconTooltip(SavegameData<?> data) {
        return AppI18n.get("atWarWith");
    }

    @Override
    protected Image getIcon() {
        return GameImage.HOI4_ICON_WAR;
    }
}
