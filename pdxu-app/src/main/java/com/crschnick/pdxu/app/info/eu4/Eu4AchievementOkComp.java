package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;

import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;

public class Eu4AchievementOkComp extends SimpleInfoComp {

    private boolean ironman;
    private boolean achievementOk;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected Image getImage() {
        return GameImage.EU4_ICON_ACHIEVEMENT;
    }

    @Override
    public Region create(SavegameData<?> data) {
        var r = super.create(data);
        if (r == null) {
            return null;
        }

        if (!achievementOk) {
            ColorAdjust grayscale = new ColorAdjust();
            grayscale.setSaturation(-1);
            r.setEffect(grayscale);
        }
        return r;
    }

    @Override
    protected boolean shouldShow() {
        return ironman;
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
        ironman = data.isIronman();
        achievementOk = NodePointer.builder().name("achievement_ok").build().getIfPresent(content.get())
                .map(Node::getBoolean)
                .orElse(false);
    }
}
