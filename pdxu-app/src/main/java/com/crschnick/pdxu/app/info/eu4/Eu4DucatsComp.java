package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.image.Image;

import java.util.concurrent.atomic.AtomicInteger;

public class Eu4DucatsComp extends SimpleInfoComp {

    private int value;
    private int loans;

    @Override
    protected void init(ArrayNode node, SavegameData<?> data) {
        AtomicInteger loansV = new AtomicInteger();
        node.getNodeForKey("countries").getNodeForKey(data.eu4().getTag().getTag()).forEach((k, v) -> {
            if (k.equals("loan")) {
                loansV.addAndGet((int) v.getNodeForKey("amount").getDouble());
            }
        });
        loans = loansV.get();

        value = (int) node.getNodeForKey("countries").getNodeForKey(data.eu4().getTag().getTag()).getNodeForKey("treasury").getDouble();
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
        return PdxuI18n.get("TREASURY") + (loans != 0 ? " / " + PdxuI18n.get("TREASURY_OWED") : "");
    }
}
