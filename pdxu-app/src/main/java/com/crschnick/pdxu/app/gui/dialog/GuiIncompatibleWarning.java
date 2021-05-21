package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.GameMod;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GuiIncompatibleWarning {

    public static boolean showIncompatibleWarning(GameInstallation installation, SavegameEntry<?, ?> entry) {
        StringBuilder builder = new StringBuilder("Selected savegame is incompatible. Launching it anyway, can cause problems.\n");
        if (SavegameCompatibility.determineForInfo(entry.getInfo()) == SavegameCompatibility.Compatbility.INCOMPATIBLE) {
            builder.append("Incompatible versions:\n")
                    .append("- Game version: ")
                    .append(installation.getVersion().toString()).append("\n")
                    .append("- Savegame version: ")
                    .append(entry.getInfo().getVersion().toString());
        }

        boolean missingMods = entry.getInfo().getMods() != null && entry.getInfo().getMods().stream()
                .map(m -> installation.getModForSavegameId(m))
                .anyMatch(Optional::isEmpty);
        if (missingMods) {
            builder.append("\nThe following Mods are missing:\n").append(entry.getInfo().getMods().stream()
                    .map(s -> {
                        var m = installation.getModForSavegameId(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        boolean missingDlc = entry.getInfo().getDlcs().stream()
                .map(m -> installation.getDlcForName(m))
                .anyMatch(Optional::isEmpty);
        if (missingDlc) {
            builder.append("\n\nThe following DLCs are missing:\n").append(entry.getInfo().getDlcs().stream()
                    .map(s -> {
                        var m = installation.getDlcForName(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        var launch = new ButtonType("Launch anyway");
        return GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.getButtonTypes().add(launch);
            alert.setTitle("Incompatible savegame");
            alert.setHeaderText(builder.toString());
        }).orElse(ButtonType.CLOSE).equals(launch);
    }

    public static Optional<Boolean> showStellarisModWarning(List<GameMod> enabledMods) {
        var launchButton = new ButtonType(PdxuI18n.get("LAUNCH"));
        var changeModsButton = new ButtonType(PdxuI18n.get("CHANGE_MODS"));

        var r = GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.getButtonTypes().add(launchButton);
            alert.getButtonTypes().add(changeModsButton);
            alert.setTitle(PdxuI18n.get("STELLARIS_INFO_TITLE"));
            alert.setHeaderText(PdxuI18n.get("STELLARIS_INFO"));

            String builder = enabledMods.stream()
                    .map(m -> "- " + m.getName())
                    .collect(Collectors.joining("\n"));
            if (enabledMods.size() == 0) {
                builder = builder + "<None>";
            }
            var text = new TextArea(builder);
            text.setPrefHeight(200);
            text.setEditable(false);

            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPadding(Insets.EMPTY);
            alert.getDialogPane().minHeightProperty().bind(text.prefHeightProperty());
        });

        if (r.isPresent()) {
            if (r.get().equals(launchButton)) return Optional.of(true);
            if (r.get().equals(changeModsButton)) return Optional.of(false);
        }
        return Optional.empty();
    }
}
