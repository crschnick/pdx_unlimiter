package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.image.Image;

public class Vic3AchievementOkComp extends SimpleInfoComp {

    private boolean achievementOk;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected Image getImage() {
        if (achievementOk) {
            return GameImage.VIC3_ICON_ACHIEVEMENT_ELIGIBLE;
        } else {
            return GameImage.VIC3_ICON_ACHIEVEMENT_INELIGIBLE;
        }
    }

    @Override
    protected boolean shouldShow() {
        return true;
    }

    @Override
    protected String getTooltip() {
        if (achievementOk) {
            return AppI18n.get("achievementEligible");
        } else {
            return AppI18n.get("achievementIneligible");
        }
    }

    @Override
    protected String getDisplayValue() {
        return null;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        achievementOk = NodePointer.builder().name("game_rules").name("setting").build().getIfPresent(content.get())
                .map(node -> node.getNodeArray().stream().anyMatch(v -> v.isValue() && v.getString().equals("achievements_allowed")))
                .orElse(false);
    }
}
