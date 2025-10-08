package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

import java.util.concurrent.atomic.AtomicInteger;

public class Eu4DucatsComp extends SimpleInfoComp {

    private int value;
    private int loans;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        AtomicInteger loansV = new AtomicInteger();
        content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTag().getTag()).forEach((k, v) -> {
            if (k.equals("loan")) {
                loansV.addAndGet((int) v.getNodeForKey("amount").getDouble());
            }
        });
        loans = loansV.get();

        value = (int) content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTag().getTag()).getNodeForKey("treasury").getDouble();
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_DUCATS;
    }

    @Override
    protected String getDisplayValue() {
        return value + (loans != 0 ? " / -" + loans : "");
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("treasury") + (loans != 0 ? " / " + AppI18n.get("treasuryOwed") : "");
    }
}
