package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu4DucatsComp extends SimpleInfoComp {

    private double value;
    private double loans;
    private double income;
    private double expense;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName());
        value = country.getNodeForKeyIfExistent("treasury").map(Node::getDouble).orElse(0.0);
        loans = country.getNodesForKey("loan").stream()
                .mapToDouble(l -> l.getNodeForKeyIfExistent("amount")
                        .map(Node::getDouble)
                        .orElse(0.0))
                .sum();
        income = country.getNodeForKeysIfExistent("ledger", "lastmonthincome")
                .map(Node::getDouble)
                .orElse(0.0);
        expense = country.getNodeForKeysIfExistent("ledger", "lastmonthexpense")
                .map(Node::getDouble)
                .orElse(0.0);
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_DUCATS;
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(value)
                + " (" + NumberFormatHelper.formatCompactSigned(income - expense) + ")"
                + (loans != 0 ? " / " + NumberFormatHelper.formatCompact(-loans) : "");
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("treasury") + " (" + AppI18n.get("monthlyBalance") + ")"
                + (loans != 0 ? " / " + AppI18n.get("treasuryOwed") : "")
                + "\n" + AppI18n.get("monthlyIncome") + ": " + NumberFormatHelper.formatDecimal(income)
                + "\n" + AppI18n.get("monthlyExpenses") + ": " + NumberFormatHelper.formatDecimal(expense);
    }
}
