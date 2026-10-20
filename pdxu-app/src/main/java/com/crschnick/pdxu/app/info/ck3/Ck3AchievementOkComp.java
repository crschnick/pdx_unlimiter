package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class Ck3AchievementOkComp extends SimpleInfoComp {

    private Boolean achievementOk;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        achievementOk = content.get()
                .getNodeForKeysIfExistent("meta_data", "can_get_achievements")
                .filter(Node::isValue)
                .map(Node::getBoolean)
                .orElse(null);
    }

    @Override
    protected boolean shouldShow() {
        return achievementOk != null;
    }

    @Override
    protected Image getImage() {
        return achievementOk ? GameImage.CK3_ICON_ACHIEVEMENT_ELIGIBLE : GameImage.CK3_ICON_ACHIEVEMENT_INELIGIBLE;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get(achievementOk ? "achievementEligible" : "achievementIneligible");
    }

    @Override
    protected String getDisplayValue() {
        return null;
    }
}
