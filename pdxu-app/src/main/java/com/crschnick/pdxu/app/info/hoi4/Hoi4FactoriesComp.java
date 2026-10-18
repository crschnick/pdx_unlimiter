package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_POWER_ICON;
import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class Hoi4FactoriesComp extends SavegameInfoComp {

    private int civilian;
    private int military;
    private int dockyards;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var tag = data.hoi4().getTag().getTag();
        content.get().getNodeForKeyIfExistent("states").ifPresent(states -> states.forEach((k, state) -> {
            if (!state.isArray()) {
                return;
            }

            var owner = state.getNodeForKeyIfExistent("owner").map(Node::getString);
            if (owner.isEmpty() || !owner.get().equals(tag)) {
                return;
            }

            state.getNodeForKeyIfExistent("buildings").ifPresent(buildings -> {
                civilian += getLevel(buildings, "industrial_complex");
                military += getLevel(buildings, "arms_factory");
                dockyards += getLevel(buildings, "dockyard");
            });
        }));
    }

    private static int getLevel(Node buildings, String type) {
        return buildings
                .getNodeForKeyIfExistent(type)
                .flatMap(b -> b.getNodeForKeyIfExistent("level"))
                .map(level -> {
                    if (level.isArray()) {
                        return level.getNodeArray().size();
                    }
                    return level.isValue() ? (int) level.getDouble() : 0;
                })
                .orElse(0);
    }

    private static Label createLabel(String text, Image icon, String tooltip) {
        var label = new Label(text, imageNode(icon, CLASS_POWER_ICON));
        label.setEllipsisString("");
        label.setMinWidth(Region.USE_PREF_SIZE);
        GuiTooltips.install(label, tooltip);
        return label;
    }

    @Override
    public Region create(SavegameData<?> data) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER);
        box.getChildren().add(createLabel(civilian + "  ", HOI4_ICON_CIVILIAN_FACTORY, AppI18n.get("civilianFactories")));
        box.getChildren().add(createLabel(military + "  ", HOI4_ICON_MILITARY_FACTORY, AppI18n.get("militaryFactories")));
        box.getChildren().add(createLabel(String.valueOf(dockyards), HOI4_ICON_DOCKYARD, AppI18n.get("dockyards")));
        return box;
    }
}
