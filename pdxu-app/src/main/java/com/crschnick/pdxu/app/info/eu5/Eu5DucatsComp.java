package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

import java.util.concurrent.atomic.AtomicInteger;

public class Eu5DucatsComp extends SimpleInfoComp {

    private int value;
    private int loans;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        loans = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "")
                .getNodeForKey("economy")
                .getNodeForKeyIfExistent("total_debt")
                .map(node -> (int) node.getDouble()).orElse(0);
        value = (int) content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "")
                .getNodeForKey("currency_data")
                .getNodeForKey("gold")
                .getDouble();
    }

    @Override
    protected Image getImage() {
        return GameImage.EU5_ICON_DUCATS;
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
