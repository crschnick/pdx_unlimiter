package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.info.SavegameInfoMultiComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.War;
import com.crschnick.pdxu.model.eu5.Eu5Tag;

import javafx.scene.image.Image;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public class Eu5WarMultiComp extends SavegameInfoMultiComp {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarComp extends Eu5DiplomacyRowComp {

        private War<Eu5Tag> war;

        @Override
        protected String getStyleClass() {
            return GuiStyle.CLASS_WAR;
        }

        @Override
        protected String getIconTooltip(SavegameData<?> data) {
            return null;
        }

        @Override
        protected Image getIcon() {
            return GameImage.EU5_ICON_WAR;
        }

        @Override
        protected List<Eu5Tag> getTags(SavegameContent content, SavegameData<?> data) {
            return war.isAttacker(data.eu5().getTag()) ? war.getDefenders() : war.getAttackers();
        }
    }

    private List<WarComp> comps;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        List<War<Eu5Tag>> wars = new ArrayList<>();
        var db = content.get().getNodeForKeys("war_manager", "database");
        if (db == null) {
            return;
        }

        db.forEach((s, node) -> {
            if (node.isValue()) {
                return;
            }

            List<Eu5Tag> attackers = new ArrayList<>();
            List<Eu5Tag> defenders = new ArrayList<>();

            var all = node.getNodeForKey("all");
            for (Node allEntry : all.getNodeArray()) {
                var countryId = allEntry.getNodeForKey("country").getLong();

                var status = allEntry.getNodeForKey("status").getString();
                if (!status.equals("Active")) {
                    continue;
                }

                if (!allEntry.hasKey("history")) {
                    continue;
                }

                var history = allEntry.getNodeForKey("history");
                if (!history.hasKey("request")) {
                    continue;
                }

                var request = history.getNodeForKey("request");
                if (!request.hasKey("side")) {
                    continue;
                }

                var side = request.getNodeForKey("side").getString();
                var attacker = !side.equals("Defender");
                if (attacker) {
                    attackers.add(Eu5Tag.getTag(data.eu5().getAllTags(), countryId));
                } else {
                    defenders.add(Eu5Tag.getTag(data.eu5().getAllTags(), countryId));
                }
            }

            if (attackers.stream()
                            .anyMatch(eu5Tag ->
                                    eu5Tag.getId() == data.eu5().getTag().getId())
                    || defenders.stream()
                            .anyMatch(eu5Tag ->
                                    eu5Tag.getId() == data.eu5().getTag().getId())) {
                var name = node.getNodeForKeys("war_name", "name").getString();
                wars.add(new War<>(name, attackers, defenders));
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
