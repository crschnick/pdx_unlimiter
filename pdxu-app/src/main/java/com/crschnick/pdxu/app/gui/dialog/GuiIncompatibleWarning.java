package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.GameMod;
import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiIncompatibleWarning {

    public static boolean showIncompatibleWarning(GameInstallation installation, SavegameEntry<?, ?> entry) {
        var info = SavegameContext.getContext(entry).getInfo();
        StringBuilder builder = new StringBuilder();
        if (SavegameCompatibility.determineForVersion(installation.getDist().getGame(), info.getData().getVersion()) == SavegameCompatibility.Compatbility.INCOMPATIBLE) {
            builder.append("Incompatible versions:\n")
                    .append("- Game version: ")
                    .append(installation.getVersion().toString()).append("\n")
                    .append("- Savegame version: ")
                    .append(info.getData().getVersion().toString());
        } else if (SavegameCompatibility.determineForModsAndDLCs(entry) == SavegameCompatibility.Compatbility.UNKNOWN) {
            builder.append("Unknown compatibility:\n")
                    .append("- Game version: ")
                    .append("Unknown").append("\n")
                    .append("- Savegame version: ")
                    .append(info.getData().getVersion().toString());
        }

        boolean missingMods = info.getData().getMods() != null && info.getData().getMods().stream()
                .map(m -> installation.getModForSavegameId(m))
                .anyMatch(Optional::isEmpty);
        if (missingMods) {
            builder.append("The following Mods are missing:\n").append(info.getData().getMods().stream()
                    .map(s -> {
                        var m = installation.getModForSavegameId(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        boolean missingDlc = info.getData().getDlcs() != null && info.getData().getDlcs().stream()
                .map(m -> installation.getDlcForSavegameId(m))
                .anyMatch(Optional::isEmpty);
        if (missingDlc) {
            builder.append("\n\nThe following DLCs are missing:\n").append(info.getData().getDlcs().stream()
                    .map(s -> {
                        var m = installation.getDlcForSavegameId(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        var text = new TextArea(builder.toString());
        text.setPrefHeight(200);
        text.setEditable(false);
        text.setPadding(Insets.EMPTY);

        var ok = new SimpleBooleanProperty();
        var modal = ModalOverlay.of("incompatibleSavegameTitle", AppDialog.dialogTextKey("incompatibleSavegameContent"));
        modal.addButton(ModalButton.cancel());
        modal.addButton(new ModalButton("launchAnyway", () -> {
            ok.set(true);
        }, true, true));
        modal.showAndWait();
        return ok.get();
    }

    public static Optional<Boolean> showNoSavedModsWarning(Game game, List<GameMod> enabledMods) {
        var val = new SimpleObjectProperty<Boolean>();
        var modal = ModalOverlay.of("modInfoTitle", Comp.of(() -> {
            var header = AppI18n.get("modInfo", game.getTranslatedFullName());
            String builder = enabledMods.stream()
                    .map(m -> "- " + m.getName().orElse(m.getModFile().getFileName().toString()))
                    .collect(Collectors.joining("\n"));
            if (enabledMods.size() == 0) {
                builder = builder + "<None>";
            }
            var text = new TextArea(header + builder);
            text.setPrefHeight(200);
            text.setEditable(false);
            return text;
        }));
        modal.addButton(new ModalButton("launch", () -> {
            val.setValue(true);
        }, true, false));
        modal.addButton(new ModalButton("changeMods", () -> {
            val.setValue(false);
        }, true, false));
        return Optional.ofNullable(val.get());
    }
}
