package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

import java.util.List;

public class Vic3PopulationComp extends SimpleInfoComp {

    private double population;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var stats = data.vic3().getPlayerCountry(content).flatMap(c -> c.getNodeForKeyIfExistent("pop_statistics"));
        for (var key : List.of("population_lower_strata", "population_middle_strata", "population_upper_strata")) {
            population += stats.flatMap(s -> s.getNodeForKeyIfExistent(key))
                    .filter(Node::isValue)
                    .map(Node::getDouble)
                    .orElse(0.0);
        }
    }

    @Override
    protected String getDisplayValue() {
        return NumberFormatHelper.formatCompact(population);
    }

    @Override
    protected Image getImage() {
        return GameImage.VIC3_ICON_POPULATION;
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("population");
    }
}
