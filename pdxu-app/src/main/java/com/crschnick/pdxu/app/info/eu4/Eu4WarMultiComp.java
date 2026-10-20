package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.info.SavegameInfoMultiComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.War;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

import javafx.scene.image.Image;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Eu4WarMultiComp extends SavegameInfoMultiComp {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarComp extends Eu4DiplomacyRowComp {

        private War<Eu4Tag> war;

        @Override
        protected String getStyleClass() {
            return GuiStyle.CLASS_WAR;
        }

        @Override
        protected String getIconTooltip(SavegameData<?> data) {
            return war.getTitle();
        }

        @Override
        protected Image getIcon() {
            return GameImage.EU4_ICON_WAR;
        }

        @Override
        protected List<Eu4Tag> getTags(SavegameContent content, SavegameData<?> data) {
            return war.isAttacker(data.eu4().getTag()) ? war.getDefenders() : war.getAttackers();
        }
    }

    private List<WarComp> comps;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var player = data.eu4().getTagName();
        var wars = new ArrayList<War<Eu4Tag>>();
        for (Node war : content.get().getNodesForKey("active_war")) {
            var title = war.getNodeForKeyIfExistent("name")
                    .map(Node::getString)
                    .orElseGet(() -> AppI18n.get("atWarWith"));
            var attackers = getParticipants(data, war, "attackers");
            var defenders = getParticipants(data, war, "defenders");
            if (attackers.stream().anyMatch(t -> t.getTag().equals(player))) {
                wars.add(new War<>(title, attackers, defenders));
            } else if (defenders.stream().anyMatch(t -> t.getTag().equals(player))) {
                wars.add(new War<>(title, defenders, attackers));
            }
        }
        comps = wars.stream().map(WarComp::new).toList();
        comps.forEach(warComp -> warComp.init(content, data));
    }

    private static List<Eu4Tag> getParticipants(SavegameData<?> data, Node war, String side) {
        return war.getNodeForKeyIfExistent(side).map(Node::getNodeArray).orElse(List.of()).stream()
                .map(n -> Eu4Tag.getTag(data.eu4().getAllTags(), n.getString()))
                .toList();
    }

    @Override
    protected List<? extends SavegameInfoComp> create(SavegameData<?> data) {
        return comps;
    }
}
