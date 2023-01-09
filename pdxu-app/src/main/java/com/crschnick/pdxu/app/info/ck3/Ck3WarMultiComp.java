package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.info.SavegameInfoMultiComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.War;
import com.crschnick.pdxu.model.ck3.Ck3Strings;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import javafx.scene.image.Image;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Ck3WarMultiComp extends SavegameInfoMultiComp {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarComp extends Ck3DiplomacyRowComp {

        private War<Ck3Tag> war;

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
            return GameImage.CK3_ICON_WAR;
        }

        @Override
        protected List<Ck3Tag> getTags(SavegameContent content, SavegameData<?> data) {
            return war.isAttacker(data.ck3().getTag()) ? war.getDefenders() : war.getAttackers();
        }
    }

    private List<WarComp> comps;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        List<War<Ck3Tag>> wars = new ArrayList<>();
        content.get().getNodeForKey("wars").getNodeForKey("active_wars").getNodeArray().forEach(v -> {
            if (v.isValue() && v.getString().equals("none")) {
                return;
            }

            var title = v.getNodeForKey("name").getString();

            List<Ck3Tag> attackers = new ArrayList<>();
            for (Node atk : v.getNodeForKey("attacker").getNodeForKey("participants").getNodeArray()) {
                var attacker = atk.getNodeForKey("character").getLong();
                Ck3Tag.getTag(data.ck3().getAllTags(), attacker).ifPresent(attackers::add);
            }

            List<Ck3Tag> defenders = new ArrayList<>();
            for (Node atk : v.getNodeForKey("defender").getNodeForKey("participants").getNodeArray()) {
                var defender = atk.getNodeForKey("character").getLong();
                Ck3Tag.getTag(data.ck3().getAllTags(), defender).ifPresent(defenders::add);
            }

            if (attackers.contains(data.ck3().getTag()) || defenders.contains(data.ck3().getTag())) {
                wars.add(new War<>(Ck3Strings.cleanCk3FormatData(title), attackers, defenders));
            }
        });
        comps = wars.stream().map(WarComp::new).toList();
        comps.forEach(warComp -> warComp.init(content, data));
    }

    @Override
    protected List<? extends SavegameInfoComp> create(SavegameData<?> data) {
        return comps;
    }
}
