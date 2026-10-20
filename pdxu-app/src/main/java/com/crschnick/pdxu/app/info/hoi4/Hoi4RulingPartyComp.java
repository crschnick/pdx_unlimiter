package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Hoi4RulingPartyComp extends SimpleInfoComp {

    private String tag;
    private String rulingParty;
    private Map<String, Double> popularities;
    private transient String partyName;
    private transient String tooltip;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        tag = data.hoi4().getTag().getTag();
        var politics =
                content.get().getNodeForKey("countries").getNodeForKey(tag).getNodeForKeyIfExistent("politics");
        rulingParty = politics.flatMap(p -> p.getNodeForKeyIfExistent("ruling_party"))
                .map(Node::getString)
                .orElse(null);
        popularities = new LinkedHashMap<>();
        politics.flatMap(p -> p.getNodeForKeyIfExistent("parties"))
                .ifPresent(parties -> parties.forEach((k, v) -> {
                    if (k != null && v.isArray()) {
                        popularities.put(
                                k,
                                v.getNodeForKeyIfExistent("popularity")
                                        .filter(Node::isValue)
                                        .map(Node::getDouble)
                                        .orElse(0.0));
                    }
                }));
    }

    private static String getPartyName(SavegameData<?> data, String tag, String ideology) {
        var key = tag + "_" + ideology + "_party";
        var name = GameLocalisation.getLocalisedValue(key, data);
        return !name.equals(key) ? name : GameLocalisation.getLocalisedValue(ideology, data);
    }

    @Override
    protected boolean shouldShow() {
        return rulingParty != null;
    }

    @Override
    protected String getDisplayValue() {
        return partyName + " " + Math.round(popularities.getOrDefault(rulingParty, 0.0)) + "%";
    }

    @Override
    protected Image getImage() {
        return switch (rulingParty) {
            case "democratic" -> GameImage.HOI4_ICON_IDEOLOGY_DEMOCRATIC;
            case "communism" -> GameImage.HOI4_ICON_IDEOLOGY_COMMUNISM;
            case "fascism" -> GameImage.HOI4_ICON_IDEOLOGY_FASCISM;
            case "neutrality" -> GameImage.HOI4_ICON_IDEOLOGY_NEUTRALITY;
            default -> GameImage.HOI4_ICON_IDEOLOGY_UNKNOWN;
        };
    }

    @Override
    protected String getTooltip() {
        return tooltip;
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (rulingParty == null) {
            return null;
        }

        partyName = getPartyName(data, tag, rulingParty);
        tooltip = AppI18n.get("rulingParty") + "\n"
                + popularities.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .map(e -> getPartyName(data, tag, e.getKey()) + ": " + Math.round(e.getValue()) + "%")
                        .collect(Collectors.joining("\n"));
        return super.create(data);
    }
}
