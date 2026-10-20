package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Hoi4ManpowerComp extends SimpleInfoComp {

    private long value;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var tag = data.hoi4().getTag().getTag();
        content.get()
                .getNodeForKeyIfExistent("states")
                .ifPresent(states -> states.forEach((k, state) -> {
                    if (!state.isArray()) {
                        return;
                    }

                    var controller = state.getNodeForKeyIfExistent("controller")
                            .or(() -> state.getNodeForKeyIfExistent("owner"))
                            .map(Node::getString);
                    if (controller.isEmpty() || !controller.get().equals(tag)) {
                        return;
                    }

                    value += state.getNodeForKeyIfExistent("manpower_pool")
                            .flatMap(pool -> pool.getNodeForKeyIfExistent("available"))
                            .filter(Node::isValue)
                            .map(available -> (long) available.getDouble())
                            .orElse(0L);
                }));
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(value);
    }

    @Override
    protected Image getImage() {
        return GameImage.HOI4_ICON_MANPOWER;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("manpower");
    }
}
