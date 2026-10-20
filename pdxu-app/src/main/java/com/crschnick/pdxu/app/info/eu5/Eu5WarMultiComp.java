package com.crschnick.pdxu.app.info.eu5;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiStyle;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoComp;
import com.crschnick.pdxu.app.info.SavegameInfoMultiComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.War;
import com.crschnick.pdxu.model.eu5.Eu5Tag;

import javafx.scene.image.Image;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Eu5WarMultiComp extends SavegameInfoMultiComp {

    private static final Pattern VARIABLE = Pattern.compile("\\$([A-Za-z_]+)\\$");

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarComp extends Eu5DiplomacyRowComp {

        private War<Eu5Tag> war;
        private String first;
        private String second;

        @Override
        protected String getStyleClass() {
            return GuiStyle.CLASS_WAR;
        }

        @Override
        protected String getIconTooltip(SavegameData<?> data) {
            if (war.getTitle() == null) {
                return AppI18n.get("atWarWith");
            }

            var template = GameLocalisation.getLocalisedValue(war.getTitle(), data);
            var matcher = VARIABLE.matcher(template);
            var result = new StringBuilder();
            while (matcher.find()) {
                var variable = matcher.group(1);
                String replacement = "";
                if (variable.startsWith("FIRST") && first != null) {
                    replacement = GameLocalisation.getLocalisedValue(first, data);
                } else if (variable.startsWith("SECOND") && second != null) {
                    replacement = GameLocalisation.getLocalisedValue(second, data);
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(result);
            return result.toString().replaceAll("\\s+", " ").trim();
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

    private List<WarComp> comps = List.of();

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var db = content.get().getNodeForKeysIfExistent("war_manager", "database");
        if (db.isEmpty()) {
            return;
        }

        var player = data.eu5().getTag().getId();
        var created = new ArrayList<WarComp>();
        db.get().forEach((s, node) -> {
            if (!node.isArray() || !node.hasKey("all")) {
                return;
            }

            var attackers = new ArrayList<Eu5Tag>();
            var defenders = new ArrayList<Eu5Tag>();
            for (Node entry : node.getNodeForKey("all").getNodeArray()) {
                var status = entry.getNodeForKeyIfExistent("status").map(Node::getString);
                var side = entry.getNodeForKeysIfExistent("history", "request", "side")
                        .map(Node::getString);
                if (status.isEmpty() || !status.get().equals("Active") || side.isEmpty()) {
                    continue;
                }

                var tag = Eu5Tag.getTag(data.eu5().getAllTags(), entry.getNodeForKey("country").getLong());
                if (side.get().equals("Defender")) {
                    defenders.add(tag);
                } else {
                    attackers.add(tag);
                }
            }

            if (attackers.stream().noneMatch(t -> t.getId() == player)
                    && defenders.stream().noneMatch(t -> t.getId() == player)) {
                return;
            }

            var warName = node.getNodeForKeyIfExistent("war_name");
            var name = warName.flatMap(n -> n.getNodeForKeyIfExistent("name"))
                    .filter(Node::isValue)
                    .map(Node::getString)
                    .orElse(null);
            var bases = warName.flatMap(n -> n.getNodeForKeyIfExistent("bases"));
            var first = bases.flatMap(b -> getBaseName(b, "First")).orElse(null);
            var second = bases.flatMap(b -> getBaseName(b, "Second")).orElse(null);
            created.add(new WarComp(new War<>(name, attackers, defenders), first, second));
        });
        comps = created;
        comps.forEach(warComp -> warComp.init(content, data));
    }

    private static Optional<String> getBaseName(Node bases, String key) {
        return bases.getNodeForKeyIfExistent(key).flatMap(base -> base.isValue()
                ? Optional.of(base.getString())
                : base.getNodeForKeyIfExistent("name").filter(Node::isValue).map(Node::getString));
    }

    @Override
    protected List<? extends SavegameInfoComp> create(SavegameData<?> data) {
        return comps;
    }
}
