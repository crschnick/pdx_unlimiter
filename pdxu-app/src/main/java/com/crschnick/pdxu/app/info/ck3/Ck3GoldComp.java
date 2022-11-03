package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

import static com.crschnick.pdxu.app.gui.game.GameImage.CK3_ICON_GOLD;

public class Ck3GoldComp extends SimpleInfoComp {

    private int value;
    private int income;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        value = data.ck3().getTag().getGold();
        income = data.ck3().getTag().getIncome();
    }

    @Override
    protected String getDisplayValue() {
        return " " + value + " / " + (income > 0 ? "+" : "") + income;
    }

    @Override
    protected Image getImage() {
        return CK3_ICON_GOLD;
    }

    @Override
    protected String getTooltip() {
        return PdxuI18n.get("TREASURY_GOLD") + " / " + PdxuI18n.get("MONTHLY_INCOME");
    }
}
