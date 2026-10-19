package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.info.InfoNodes;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
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
        var tag = data.eu4().getTagName();
        adm = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(tag)
                .getNodeForKey("powers")
                .getNodeArray()
                .get(0)
                .getInteger();
        dip = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(tag)
                .getNodeForKey("powers")
                .getNodeArray()
                .get(1)
                .getInteger();
        mil = content.get()
                .getNodeForKey("countries")
                .getNodeForKey(tag)
                .getNodeForKey("powers")
                .getNodeArray()
                .get(2)
                .getInteger();
    }

    @Override
    public Region create(SavegameData<?> data) {
        return InfoNodes.createValueRow(List.of(
                new InfoNodes.ValueEntry(String.valueOf(adm), EU4_ICON_ADM, AppI18n.get("administrativePower")),
                new InfoNodes.ValueEntry(String.valueOf(dip), EU4_ICON_DIP, AppI18n.get("diplomaticPower")),
                new InfoNodes.ValueEntry(String.valueOf(mil), EU4_ICON_MIL, AppI18n.get("militaryPower"))));
    }
}
