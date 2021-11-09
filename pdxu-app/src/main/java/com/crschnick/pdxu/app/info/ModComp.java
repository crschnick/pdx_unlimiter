package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.gui.GuiTooltips;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public class ModComp extends SavegameInfoComp {

    private List<String> mods;
    private GameInstallation installation;

    @Override
    protected void init(ArrayNode node, SavegameData data) {
        this.mods = new ArrayList<>();
        if (data.getMods() != null && data.getMods().size() > 0) {
            this.mods.addAll(data.getMods());
        }

        this.installation = data.installation();
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (mods.size() == 0) {
            return null;
        }

        Label label = new Label(PdxuI18n.get("MODS") + " (" + this.mods.size() + ")");
        label.setGraphic(new FontIcon());
        label.getStyleClass().add(CLASS_CONTENT);
        GuiTooltips.install(label, getTooltip());
        boolean missing = this.mods.stream()
                .map(m -> installation.getModForSavegameId(m))
                .anyMatch(Optional::isEmpty);
        label.getStyleClass().add(missing ? CLASS_INCOMPATIBLE : CLASS_COMPATIBLE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private String getTooltip() {
        return PdxuI18n.get("MODS_REQUIRED") + ":\n" +
                mods.stream()
                        .map(s -> {
                            var m = installation.getModForSavegameId(s);
                            return "- " + (m.isPresent() ? m.get().getName() : s + " (" + PdxuI18n.get("MISSING") + ")");
                        })
                        .collect(Collectors.joining("\n"));
    }
}
