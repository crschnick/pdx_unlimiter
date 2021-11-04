package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.GameVersion;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_COMPATIBLE;
import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_INCOMPATIBLE;

public class VersionComp extends SavegameInfoComp {

    private GameVersion version;

    @Override
    protected void init(ArrayNode node, SavegameData data) {
        this.version = data.getVersion();
    }

    @Override
    public Region create() {
        Label label = null;
        switch (SavegameCompatibility.determineForInfo(version)) {
            case COMPATIBLE -> {
                label = new Label(version.toString());
                GuiTooltips.install(label, PdxuI18n.get("COMPATIBLE"));
                label.getStyleClass().add(CLASS_COMPATIBLE);
            }
            case INCOMPATIBLE -> {
                label = new Label(version.toString());
                GuiTooltips.install(label, PdxuI18n.get("INCOMPATIBLE"));
                label.getStyleClass().add(CLASS_INCOMPATIBLE);
            }
            case UNKNOWN -> {
                label = new Label(version.toString());
                GuiTooltips.install(label, PdxuI18n.get("UNKNOWN_COMPATIBILITY"));
                label.getStyleClass().add("unknown-compatible");
            }
        }
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
