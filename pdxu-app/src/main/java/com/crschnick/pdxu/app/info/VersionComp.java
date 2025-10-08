package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;

import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.GameVersion;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_COMPATIBLE;
import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_INCOMPATIBLE;

public class VersionComp extends SavegameInfoComp {

    private GameVersion version;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        this.version = data.getVersion();
    }

    @Override
    public Region create(SavegameData<?> data) {
        Label label = null;
        switch (SavegameCompatibility.determineForVersion(data.installation().getDist().getGame(), version)) {
            case COMPATIBLE -> {
                label = new Label(version.toString());
                GuiTooltips.install(label, AppI18n.get("compatible"));
                label.getStyleClass().add(CLASS_COMPATIBLE);
            }
            case INCOMPATIBLE -> {
                label = new Label(version.toString());
                GuiTooltips.install(label, AppI18n.get("incompatible"));
                label.getStyleClass().add(CLASS_INCOMPATIBLE);
            }
            case UNKNOWN -> {
                label = new Label(version.toString());
                GuiTooltips.install(label, AppI18n.get("unknownCompatibility"));
                label.getStyleClass().add("unknown-compatible");
            }
        }
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
