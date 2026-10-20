package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

import java.util.List;

public class Eu4LegitimacyComp extends SimpleInfoComp {

    private String type;
    private int value;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var country = content.get().getNodeForKey("countries").getNodeForKey(data.eu4().getTagName());
        var government = country.getNodeForKeysIfExistent("government", "government")
                .map(Node::getString)
                .orElse("");
        var reforms = country.getNodeForKeysIfExistent("government", "reform_stack", "reforms")
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getString)
                .toList();

        String key;
        if (reforms.contains("celestial_empire")) {
            type = "meritocracy";
            key = "meritocracy";
        } else {
            switch (government) {
                case "monarchy" -> {
                    type = "legitimacy";
                    key = "legitimacy";
                }
                case "republic" -> {
                    type = "republicanTradition";
                    key = "republican_tradition";
                }
                case "theocracy" -> {
                    type = "devotion";
                    key = "devotion";
                }
                case "tribal" -> {
                    var horde = country.hasKey("horde_unity");
                    type = horde ? "hordeUnity" : "legitimacy";
                    key = horde ? "horde_unity" : "legitimacy";
                }
                default -> {
                    type = null;
                    key = null;
                }
            }
        }

        value = key != null
                ? country.getNodeForKeyIfExistent(key)
                        .map(Node::getDouble)
                        .orElse(0.0)
                        .intValue()
                : 0;
    }

    @Override
    protected boolean shouldShow() {
        return type != null;
    }

    @Override
    protected String getDisplayValue() {
        return String.valueOf(value);
    }

    @Override
    protected Image getImage() {
        return switch (type) {
            case "republicanTradition" -> GameImage.EU4_ICON_REPUBLICAN_TRADITION;
            case "devotion" -> GameImage.EU4_ICON_DEVOTION;
            case "hordeUnity" -> GameImage.EU4_ICON_HORDE_UNITY;
            case "meritocracy" -> GameImage.EU4_ICON_MERITOCRACY;
            default -> GameImage.EU4_ICON_LEGITIMACY;
        };
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get(type);
    }
}
