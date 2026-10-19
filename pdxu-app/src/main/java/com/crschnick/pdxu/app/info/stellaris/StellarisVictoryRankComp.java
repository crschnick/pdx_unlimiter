package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

public class StellarisVictoryRankComp extends SimpleInfoComp {

    private int rank;
    private double score;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        rank = (int) data.stellaris().getPlayerValue(content, "victory_rank");
        score = data.stellaris().getPlayerValue(content, "victory_score");
    }

    @Override
    protected String getDisplayValue() {
        return "#" + rank;
    }

    @Override
    protected Image getImage() {
        return GameImage.STELLARIS_ICON_VICTORY;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("victoryRank") + " (" + AppI18n.get("victoryScore") + ": "
                + NumberFormatHelper.formatCompact(score) + ")";
    }
}
