package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.GameVersion;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

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
        String text;
        String tooltip;
        String styleClass;
        switch (SavegameCompatibility.determineForVersion(
                data.installation().getDist().getGame(), version)) {
            case COMPATIBLE -> {
                text = "v" + version.toString();
                tooltip = AppI18n.get("compatible");
                styleClass = CLASS_COMPATIBLE;
            }
            case INCOMPATIBLE -> {
                text = "v" + version.toString() + " (!)";
                tooltip = AppI18n.get("incompatible");
                styleClass = CLASS_INCOMPATIBLE;
            }
            default -> {
                text = "v" + version.toString() + " (?)";
                tooltip = AppI18n.get("unknownCompatibility");
                styleClass = "unknown-compatible";
            }
        }

        var label = new Label(text);
        label.setMinWidth(Region.USE_PREF_SIZE);
        label.setEllipsisString("");

        var stack = new StackPane(label);
        stack.setAlignment(Pos.CENTER);
        stack.getStyleClass().add(styleClass);
        GuiTooltips.install(stack, tooltip);
        return stack;
    }
}
