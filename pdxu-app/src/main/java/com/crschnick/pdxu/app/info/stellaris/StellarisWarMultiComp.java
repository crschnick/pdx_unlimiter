package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.info.SavegameInfoMultiComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.War;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class StellarisWarMultiComp extends SavegameInfoMultiComp {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarComp extends StellarisDiplomacyRowComp {

        private War<StellarisTag> war;

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
            return GameImage.STELLARIS_ICON_WAR;
        }

        @Override
        protected List<StellarisTag> getTags(SavegameContent content, SavegameData<?> data) {
            return war.isAttacker(data.stellaris().getTag()) ? war.getDefenders() : war.getAttackers();
        }
    }

    private List<WarComp> comps;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        List<War<StellarisTag>> wars = new ArrayList<>();
        for (Node war : content.get().getNodeForKey("war").getArrayNode().getNodeArray()) {
            if (war.isValue()) {
                continue;
            }


            String title = war.getNodeForKeyIfExistent("key").map(Node::getString).orElse("MISSING NAME");
            boolean isAttacker = false;
            List<StellarisTag> attackers = new ArrayList<>();
            if (war.hasKey("attackers")) {
                for (Node atk : war.getNodeForKey("attackers").getNodeArray()) {
                    var attacker = atk.getNodeForKey("country").getLong();
                    var tag = StellarisTag.getTag(data.stellaris().getAllTags(), attacker);
                    tag.ifPresent(attackers::add);
                    if (attacker == 0) {
                        isAttacker = true;
                    }
                }
            }

            boolean isDefender = false;
            List<StellarisTag> defenders = new ArrayList<>();
            if (war.hasKey("defenders")) {
                for (Node def : war.getNodeForKey("defenders").getNodeArray()) {
                    var defender = def.getNodeForKey("country").getLong();
                    var tag = StellarisTag.getTag(data.stellaris().getAllTags(), defender);
                    tag.ifPresent(defenders::add);
                    if (defender == 0) {
                        isDefender = true;
                    }
                }
            }
            if (isAttacker) {
                wars.add(new War<>(title, attackers, defenders));
            } else if (isDefender) {
                wars.add(new War<>(title, defenders, attackers));
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
