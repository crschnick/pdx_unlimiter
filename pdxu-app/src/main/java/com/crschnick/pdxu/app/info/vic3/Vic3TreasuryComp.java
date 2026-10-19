package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

import java.util.Optional;

public class Vic3TreasuryComp extends SimpleInfoComp {

    private double money;
    private double debt;
    private double credit;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var budget = data.vic3().getPlayerCountry(content).flatMap(c -> c.getNodeForKeyIfExistent("budget"));
        money = getValue(budget, "money");
        debt = getValue(budget, "principal");
        credit = getValue(budget, "credit");
    }

    private static double getValue(Optional<Node> budget, String key) {
        return budget.flatMap(b -> b.getNodeForKeyIfExistent(key))
                .filter(Node::isValue)
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(debt > 0 ? -debt : money);
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_MONEY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get(debt > 0 ? "vic3Debt" : "vic3Treasury") + "\n" + AppI18n.get("vic3Credit") + ": "
                + NumberFormatHelper.formatCompact(credit);
    }
}
