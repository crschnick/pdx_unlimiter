package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.info.InfoNodes;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.util.NumberFormatHelper;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.layout.Region;

import java.util.List;

import static com.crschnick.pdxu.app.gui.game.GameImage.*;

public class StellarisPowerComp extends SavegameInfoComp {

    private double military;
    private double economy;
    private double tech;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        military = data.stellaris().getPlayerValue(content, "military_power");
        economy = data.stellaris().getPlayerValue(content, "economy_power");
        tech = data.stellaris().getPlayerValue(content, "tech_power");
    }

    @Override
    public Region create(SavegameData<?> data) {
        return InfoNodes.createValueRow(List.of(
                new InfoNodes.ValueEntry(
                        NumberFormatHelper.formatCompact(military),
                        STELLARIS_ICON_MILITARY_POWER,
                        AppI18n.get("militaryPower")),
                new InfoNodes.ValueEntry(
                        NumberFormatHelper.formatCompact(economy),
                        STELLARIS_ICON_ECONOMY_POWER,
                        AppI18n.get("economyPower")),
                new InfoNodes.ValueEntry(
                        NumberFormatHelper.formatCompact(tech), STELLARIS_ICON_TECH_POWER, AppI18n.get("techPower"))));
    }
}
