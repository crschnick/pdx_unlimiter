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
        protected String getIconTooltip(SavegameData<?> data) {
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
        var player = data.ck3().getTag();
        var wars = new ArrayList<War<Ck3Tag>>();
        content.get().getNodeForKeysIfExistent("wars", "active_wars").ifPresent(n -> {
            for (var war : n.getNodeArray()) {
                if (!war.isArray()) {
                    continue;
                }

                var attackers = getParticipants(data, war, "attacker");
                var defenders = getParticipants(data, war, "defender");
                if (attackers.contains(player) || defenders.contains(player)) {
                    var title = Ck3Strings.cleanCk3FormatData(
                            war.getNodeForKey("name").getString());
                    wars.add(new War<>(title, attackers, defenders));
                }
            }
        });
        comps = wars.stream().map(WarComp::new).toList();
        comps.forEach(warComp -> warComp.init(content, data));
    }

    private static List<Ck3Tag> getParticipants(SavegameData<?> data, Node war, String side) {
        var participants = new ArrayList<Ck3Tag>();
        war.getNodeForKeysIfExistent(side, "participants").ifPresent(n -> {
            for (var participant : n.getNodeArray()) {
                var id = participant.getNodeForKey("character").getLong();
                Ck3Tag.getTag(data.ck3().getAllTags(), id).ifPresent(participants::add);
            }
        });
        return participants;
    }

    @Override
    protected List<? extends SavegameInfoComp> create(SavegameData<?> data) {
        return comps;
    }
}
