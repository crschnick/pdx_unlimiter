package com.crschnick.pdxu.app.info.eu4;

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
        protected String getTooltip() {
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
        List<War<Eu4Tag>> wars = new ArrayList<>();
        for (Node war : content.get().getNodesForKey("active_war")) {
            String title = war.getNodeForKeyIfExistent("name").map(Node::getString).orElse("MISSING NAME");
            boolean isAttacker = false;
            List<Eu4Tag> attackers = new ArrayList<>();
            if (war.hasKey("attackers")) {
                for (Node atk : war.getNodeForKey("attackers").getNodeArray()) {
                    String attacker = atk.getString();
                    attackers.add(Eu4Tag.getTag(data.eu4().getAllTags(), attacker));
                    if (attacker.equals(data.eu4().getTagName())) {
                        isAttacker = true;
                    }
                }
            }

            boolean isDefender = false;
            List<Eu4Tag> defenders = new ArrayList<>();
            if (war.hasKey("defenders")) {
                for (Node def : war.getNodeForKey("defenders").getNodeArray()) {
                    String defender = def.getString();
                    defenders.add(Eu4Tag.getTag(data.eu4().getAllTags(), defender));
                    if (defender.equals(data.eu4().getTagName())) {
                        isDefender = true;
                    }
                }
            }
            if (isAttacker) {
                wars.add(new War<Eu4Tag>(title, attackers, defenders));
            } else if (isDefender) {
                wars.add(new War<Eu4Tag>(title, defenders, attackers));
            }
        }
        comps = wars.stream().map(WarComp::new).toList();
        comps.forEach(warComp -> warComp.init(content, data));
    }

    @Override
    protected List<? extends SavegameInfoComp> create(SavegameData<?> data) {
        return comps;
    }
}
