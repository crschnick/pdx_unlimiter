package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_POWER_ICON;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Eu4PowersComp extends SavegameInfoComp {

    private int adm;
    private int dip;
    private int mil;

    @Override
    protected void init(ArrayNode node, SavegameData<?> data) {
        var tag = data.eu4().getTagName();
        adm = node.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("powers").getNodeArray().get(0).getInteger();
        dip = node.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("powers").getNodeArray().get(1).getInteger();
        mil = node.getNodeForKey("countries").getNodeForKey(tag).getNodeForKey("powers").getNodeArray().get(2).getInteger();
    }

    @Override
    public Region create() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        Label admL = new Label(adm + "  ", imageNode(EU4_ICON_ADM, CLASS_POWER_ICON));
        box.getChildren().add(admL);

        Label dipL = new Label(dip + "  ", imageNode(EU4_ICON_DIP, CLASS_POWER_ICON));
        box.getChildren().add(dipL);

        Label milL = new Label(String.valueOf(mil), imageNode(EU4_ICON_MIL, CLASS_POWER_ICON));
        box.getChildren().add(milL);
        return box;
    }
}
