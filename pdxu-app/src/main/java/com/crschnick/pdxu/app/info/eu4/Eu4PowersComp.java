package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.info.InfoNodes;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Eu4PowersComp extends SavegameInfoComp {

    private int adm;
    private int dip;
    private int mil;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var powers = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(data.eu4().getTagName())
                .getNodeForKey("powers")
                .getNodeArray();
        adm = getPower(powers, 0);
        dip = getPower(powers, 1);
        mil = getPower(powers, 2);
    }

    private static int getPower(List<Node> powers, int index) {
        return index < powers.size() ? powers.get(index).getInteger() : 0;
    }

    @Override
    public Region create(SavegameData<?> data) {
        return InfoNodes.createValueRow(List.of(
                new InfoNodes.ValueEntry(String.valueOf(adm), EU4_ICON_ADM, AppI18n.get("administrativePower")),
                new InfoNodes.ValueEntry(String.valueOf(dip), EU4_ICON_DIP, AppI18n.get("diplomaticPower")),
                new InfoNodes.ValueEntry(String.valueOf(mil), EU4_ICON_MIL, AppI18n.get("militaryPower"))));
    }
}
