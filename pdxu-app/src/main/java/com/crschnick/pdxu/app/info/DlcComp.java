package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.gui.GuiTooltips;

import com.crschnick.pdxu.io.savegame.SavegameContent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public class DlcComp extends SavegameInfoComp {

    private List<String> dlcs;

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        this.dlcs = new ArrayList<>();
        if (data.getDlcs() != null && data.getDlcs().size() > 0) {
            this.dlcs.addAll(data.getDlcs());
        }
    }

    @Override
    public Region create(SavegameData<?> data) {
        if (dlcs.size() == 0) {
            return null;
        }

        Label label = new Label(AppI18n.get("dlcs") + " (" + this.dlcs.size() + ")");
        label.setGraphic(new FontIcon());
        label.getStyleClass().add(CLASS_CONTENT);

        var tooltip = AppI18n.get("dlcsRequired") + ":\n" +
                dlcs.stream()
                        .map(s -> {
                            var m = data.installation().getDlcForSavegameId(s);
                            return "- " + (m.isPresent() ? m.get().getName() : s + " (" + AppI18n.get("missing") + ")");
                        })
                        .collect(Collectors.joining("\n"));
        GuiTooltips.install(label, tooltip);

        boolean missing = this.dlcs.stream()
                .map(m -> data.installation().getDlcForSavegameId(m))
                .anyMatch(Optional::isEmpty);
        label.getStyleClass().add(missing ? CLASS_INCOMPATIBLE : CLASS_COMPATIBLE);
        label.setAlignment(Pos.CENTER);
        return label;
    }
}
