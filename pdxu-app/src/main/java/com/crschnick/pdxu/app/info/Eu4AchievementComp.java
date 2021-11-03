package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import javafx.scene.image.Image;

public class Eu4AchievementComp extends SimpleInfoComp {

    private boolean achievementOk;

    public Eu4AchievementComp(ArrayNode node) {
        super(node);
    }

    @Override
    protected void init(ArrayNode node) {
        achievementOk = NodePointer.builder().name("achievement_ok").build().getIfPresent(node)
                .map(Node::getBoolean)
                .orElse(false);
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_ACHIEVEMENT;
    }

    @Override
    protected String getTooltip() {
        if (achievementOk) {
            return PdxuI18n.get("ACHIEVEMENT_ELIGIBLE");
        } else {
            return PdxuI18n.get("ACHIEVEMENT_INELIGIBLE");
        }
    }

    @Override
    protected String getDisplayValue() {
        return null;
    }
}
