package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Eu5DucatsComp extends SimpleInfoComp {

    private double value;
    private double loans;
    private double income;
    private double expense;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "");
        var economy = country.getNodeForKey("economy");
        loans = economy.getNodeForKeyIfExistent("total_debt").map(Node::getDouble).orElse(0.0);
        income = economy.getNodeForKeyIfExistent("income").map(Node::getDouble).orElse(0.0);
        expense = economy.getNodeForKeyIfExistent("expense").map(Node::getDouble).orElse(0.0);
        value = country.getNodeForKey("currency_data").getNodeForKey("gold").getDouble();
    }

    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_DUCATS;
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
