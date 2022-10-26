package com.crschnick.pdxu.app.gui.dialog;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.GameMod;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.SavegameCompatibility;
import com.crschnick.pdxu.app.savegame.SavegameContext;
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
        var info = SavegameContext.getContext(entry).getInfo();
        StringBuilder builder = new StringBuilder();
        if (SavegameCompatibility.determineForVersion(info.getData().getVersion()) == SavegameCompatibility.Compatbility.INCOMPATIBLE) {
            builder.append("Incompatible versions:\n")
                    .append("- Game version: ")
                    .append(installation.getVersion().toString()).append("\n")
                    .append("- Savegame version: ")
                    .append(info.getData().getVersion().toString());
        } else if (SavegameCompatibility.determineForEntry(entry) == SavegameCompatibility.Compatbility.UNKNOWN) {
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

        boolean missingDlc = info.getData().getDlcs().stream()
                .map(m -> installation.getDlcForName(m))
                .anyMatch(Optional::isEmpty);
        if (missingDlc) {
            builder.append("\n\nThe following DLCs are missing:\n").append(info.getData().getDlcs().stream()
                    .map(s -> {
                        var m = installation.getDlcForName(s);
                        return (m.isPresent() ? null : "- " + s);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("\n")));
        }

        var text = new TextArea(builder.toString());
        text.setPrefHeight(200);
        text.setEditable(false);
        text.setPadding(Insets.EMPTY);

        var launch = new ButtonType("Launch anyway");
        return GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.getButtonTypes().add(launch);
            alert.setTitle("Incompatible savegame");
            alert.setHeaderText("Selected savegame may be incompatible. Launching it anyway, can cause problems");
            alert.getDialogPane().setContent(text);
        }).orElse(ButtonType.CLOSE).equals(launch);
    }

    public static Optional<Boolean> showNoSavedModsWarning(Game game, List<GameMod> enabledMods) {
        var launchButton = new ButtonType(PdxuI18n.get("LAUNCH"));
        var changeModsButton = new ButtonType(PdxuI18n.get("CHANGE_MODS"));

        var r = GuiDialogHelper.showBlockingAlert(alert -> {
            alert.setAlertType(Alert.AlertType.WARNING);
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.CLOSE);
            alert.getButtonTypes().add(launchButton);
            if (GameDistLauncher.canChangeMods(game)) {
                alert.getButtonTypes().add(changeModsButton);
            }
            alert.setTitle(PdxuI18n.get("MOD_INFO_TITLE", game.getTranslatedFullName()));
            alert.setHeaderText(PdxuI18n.get("MOD_INFO", game.getTranslatedFullName()));

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
