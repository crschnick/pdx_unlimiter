package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Ck2GoldComp extends SimpleInfoComp {

    private double value;
    private double income;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var character = data.ck2().getPlayerCharacter(content);
        value = character.flatMap(c -> c.getNodeForKeyIfExistent("wealth"))
                .map(Node::getDouble)
                .orElse(0.0);
        income = character.flatMap(c -> c.getNodeForKeyIfExistent("emi"))
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(value) + " / " + NumberFormatHelper.formatCompactSigned(income);
    }

    @Override
    protected Image getImage() {
        return GameImage.CK2_ICON_GOLD;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("treasuryGold") + " / " + AppI18n.get("monthlyIncome");
    }
}
