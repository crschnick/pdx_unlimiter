package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class Eu5LegitimacyComp extends SimpleInfoComp {

    private String governmentType;
    private int value;
    private transient String tooltip;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get()
                .getNodeForKey("countries")
                .getNodeForKey("database")
                .getNodeForKey(data.eu5().getTag().getId() + "");
        governmentType = country.getNodeForKeysIfExistent("government", "type")
                .map(Node::getString)
                .orElse("monarchy");
        value = country.getNodeForKey("currency_data")
                .getNodeForKeyIfExistent("government_power")
                .map(Node::getDouble)
                .orElse(0.0)
                .intValue();
    }

    private String getPowerKey() {
        return switch (governmentType) {
            case "republic" -> "republican_tradition";
            case "theocracy" -> "devotion";
            case "steppe_horde" -> "horde_unity";
            case "tribe" -> "tribal_cohesion";
            default -> "legitimacy";
        };
    }

    @Override
    public Region create(SavegameData<?> data) {
        tooltip = GameLocalisation.getLocalisedValue(getPowerKey(), data);
        return super.create(data);
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(value);
    }

    @Override
    protected Image getImage() {
        return switch (governmentType) {
            case "republic" -> GameImage.EU5_ICON_REPUBLICAN_TRADITION;
            case "theocracy" -> GameImage.EU5_ICON_DEVOTION;
            case "steppe_horde" -> GameImage.EU5_ICON_HORDE_UNITY;
            case "tribe" -> GameImage.EU5_ICON_TRIBAL_COHESION;
            default -> GameImage.EU5_ICON_LEGITIMACY;
        };
    }

    @Override
    protected String getTooltip() {
        return tooltip;
    }
}
