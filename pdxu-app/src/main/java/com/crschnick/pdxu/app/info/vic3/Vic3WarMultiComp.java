package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.info.SavegameInfoMultiComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Vic3WarMultiComp extends SavegameInfoMultiComp {

    @NoArgsConstructor
    public static class WarComp extends Vic3DiplomacyRowComp {

        private String title;

        public WarComp(String title, List<Country> enemies) {
            this.title = title;
            this.tags = enemies;
        }

        @Override
        protected void init(SavegameContent content, SavegameData<?> data) {}

        @Override
        protected String getStyleClass() {
            return GuiStyle.CLASS_WAR;
        }

        @Override
        protected String getIconTooltip(SavegameData<?> data) {
            return title;
        }

        @Override
        protected Image getIcon() {
            return GameImage.VIC3_ICON_WAR;
        }
    }

    private List<WarComp> comps;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        comps = new ArrayList<>();
        var playerId = data.vic3().getTag().getId();
        var countries = Vic3DiplomacyRowComp.getCountries(content);
        var plays = content.get()
                .getNodeForKeyIfExistent("diplomatic_plays")
                .flatMap(dp -> dp.getNodeForKeyIfExistent("database"));
        var wars = content.get()
                .getNodeForKeyIfExistent("war_manager")
                .flatMap(wm -> wm.getNodeForKeyIfExistent("database"));
        if (wars.isEmpty() || countries.isEmpty() || plays.isEmpty()) {
            return;
        }

        wars.get().forEach((id, war) -> {
            if (!war.isArray() || war.hasKey("dead")) {
                return;
            }

            var play = war.getNodeForKeyIfExistent("diplomatic_play")
                    .filter(Node::isValue)
                    .flatMap(p -> plays.get().getNodeForKeyIfExistent(p.getString()))
                    .filter(Node::isArray);
            if (play.isEmpty()) {
                return;
            }

            var attackers = getSide(play.get(), "initiator", "initiators");
            var defenders = getSide(play.get(), "target", "targets");
            List<String> enemies;
            if (attackers.contains(playerId)) {
                enemies = defenders;
            } else if (defenders.contains(playerId)) {
                enemies = attackers;
            } else {
                return;
            }

            var participants = enemies.stream()
                    .map(enemy -> Vic3DiplomacyRowComp.getCountry(data, countries.get(), enemy))
                    .flatMap(Optional::stream)
                    .toList();
            var title = Optional.ofNullable(Vic3Names.resolve(
                            data, war.getNodeForKeyIfExistent("name").orElse(null)))
                    .orElseGet(() -> createDefaultTitle(data, countries.get(), attackers, defenders));
            comps.add(new WarComp(title, participants));
        });
    }

    private static String createDefaultTitle(
            SavegameData<?> data, Node countries, List<String> attackers, List<String> defenders) {
        var initiator = attackers.stream()
                .findFirst()
                .flatMap(id -> Vic3DiplomacyRowComp.getCountry(data, countries, id))
                .map(Vic3DiplomacyRowComp.Country::getName)
                .orElse("?");
        var target = defenders.stream()
                .findFirst()
                .flatMap(id -> Vic3DiplomacyRowComp.getCountry(data, countries, id))
                .map(Vic3DiplomacyRowComp.Country::getName)
                .orElse("?");
        return GameLocalisation.getLocalisedValue("WAR_NAME_FORMAT_DEFAULT", data)
                .replace("$INITIATOR$", initiator)
                .replace("$TARGET$", target);
    }

    private static List<String> getSide(Node play, String leaderKey, String othersKey) {
        var side = new ArrayList<String>();
        play.getNodeForKeyIfExistent(leaderKey)
                .filter(Node::isValue)
                .map(Node::getString)
                .ifPresent(side::add);
        play.getNodeForKeyIfExistent(othersKey).filter(Node::isArray).ifPresent(others -> {
            for (var other : others.getNodeArray()) {
                if (other.isValue() && !side.contains(other.getString())) {
                    side.add(other.getString());
                }
            }
        });
        return side;
    }

    @Override
    protected List<? extends SavegameInfoComp> create(SavegameData<?> data) {
        return comps;
    }
}
