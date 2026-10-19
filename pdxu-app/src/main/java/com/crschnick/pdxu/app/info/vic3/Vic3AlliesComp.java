package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Set;

public class Vic3AlliesComp extends Vic3DiplomacyRowComp {

    private static final Set<String> ALLIANCE_PACTS = Set.of("alliance", "defensive_pact");

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        tags = new ArrayList<>();
        var playerId = data.vic3().getTag().getId();
        var countries = getCountries(content);
        var pacts = content.get()
                .getNodeForKeyIfExistent("pacts")
                .flatMap(p -> p.getNodeForKeyIfExistent("database"));
        if (countries.isEmpty() || pacts.isEmpty()) {
            return;
        }

        var allyIds = new ArrayList<String>();
        pacts.get().forEach((id, pact) -> {
            if (!pact.isArray()) {
                return;
            }

            var action = pact.getNodeForKeyIfExistent("action").filter(Node::isValue).map(Node::getString);
            if (action.isEmpty() || !ALLIANCE_PACTS.contains(action.get())) {
                return;
            }

            var targets = pact.getNodeForKeyIfExistent("targets");
            var first = targets.flatMap(t -> t.getNodeForKeyIfExistent("first"))
                    .filter(Node::isValue)
                    .map(Node::getString)
                    .orElse(null);
            var second = targets.flatMap(t -> t.getNodeForKeyIfExistent("second"))
                    .filter(Node::isValue)
                    .map(Node::getString)
                    .orElse(null);
            var ally = playerId.equals(first) ? second : playerId.equals(second) ? first : null;
            if (ally != null && !allyIds.contains(ally)) {
                allyIds.add(ally);
            }
        });

        for (var ally : allyIds) {
            getCountry(data, countries.get(), ally).ifPresent(tags::add);
        }
    }

    @Override
    protected String getStyleClass() {
        return GuiStyle.CLASS_ALLIANCE;
    }

    @Override
    protected String getIconTooltip(SavegameData<?> data) {
        return AppI18n.get("allies");
    }

    @Override
    protected Image getIcon() {
        return GameImage.VIC3_ICON_ALLIANCE;
    }
}
