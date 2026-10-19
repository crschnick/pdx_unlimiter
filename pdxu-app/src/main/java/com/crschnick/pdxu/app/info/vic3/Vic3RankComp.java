package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.app.installation.GameLocalisation;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import javafx.scene.image.Image;
import javafx.scene.layout.Region;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Vic3RankComp extends SimpleInfoComp {

    private String rank;
    private transient String rankName;

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        rank = content.get()
                .getNodeForKeyIfExistent("meta_data")
                .flatMap(m -> m.getNodeForKeyIfExistent("rank"))
                .filter(Node::isValue)
                .map(Node::getString)
                .orElse(null);
    }

    @Override
    protected boolean shouldShow() {
        return rank != null;
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (rank == null) {
            return null;
        }

        var localised = GameLocalisation.getLocalisedValue(rank, data);
        rankName = !localised.equals(rank)
                ? localised
                : Arrays.stream(rank.split("_"))
                        .filter(s -> !s.isEmpty())
                        .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                        .collect(Collectors.joining(" "));
        return super.create(data);
    }

    @Override
    protected String getDisplayValue() {
        return rankName;
    }

    @Override
    protected Image getImage() {
        return switch (rank) {
            case "great_power", "super_power" -> GameImage.VIC3_ICON_RANK_4;
            case "major_power", "unrecognized_major_power" -> GameImage.VIC3_ICON_RANK_3;
            case "minor_power", "unrecognized_regional_power" -> GameImage.VIC3_ICON_RANK_2;
            default -> GameImage.VIC3_ICON_RANK_1;
        };
    }

    @Override
    protected String getTooltip() {
        return AppI18n.get("countryRank");
    }
}
