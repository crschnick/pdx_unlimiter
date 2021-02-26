package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.nio.file.Files;

import static com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper.createAlert;

public class Eu4SeHelper {

    public static void showEnabledDialog() {
        Platform.runLater(() -> {
            Alert alert = createAlert();
            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.setTitle("Eu4SaveEditor");
            alert.setHeaderText("""
                    You enabled the Eu4SaveEditor. It will be automatically downloaded
                    and installed the next time you start the Pdx-Unlimiter.
                    """);
            alert.showAndWait();
        });
    }

    private static void showUsageDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);

        var open = new ButtonType("Visit Eu4SaveEditor", ButtonBar.ButtonData.APPLY);
        alert.getButtonTypes().add(open);
        Button val = (Button) alert.getDialogPane().lookupButton(open);
        val.setOnAction(e -> {
            ThreadHelper.browse("https://github.com/Osallek/Eu4SaveEditor");
        });

        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Eu4SaveEditor");
        alert.setHeaderText("""
                The Eu4SaveEditor is a very user friendly editor with a
                map based interface that allows you to easily edit parts of your savegames.
                """);
        alert.setContentText("""
                You can enable this functionality in the settings menu.
                """);
        alert.showAndWait();
    }

    public static boolean shouldShowButton(SavegameEntry<?, ?> entry, SavegameInfo<?> info) {
        if (info.isBinary()) {
            return false;
        }

        if (SavegameStorage.EU4 != null && SavegameStorage.EU4.contains(entry)) {
            Eu4SavegameInfo eu4i = (Eu4SavegameInfo) info;
            return !eu4i.isRandomNewWorld();
        } else {
            return false;
        }
    }

    public static void open(SavegameEntry<?, ?> entry) {
        if (!PdxuInstallation.getInstance().isEu4SaveEditorInstalled()) {
            showUsageDialog();
            return;
        }

        ThreadHelper.create("eu4se", true, () -> {
            // Create mod dir in case no mods are installed
            try {
                Files.createDirectory(GameInstallation.EU4.getUserPath().resolve("mod"));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
                return;
            }

            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) entry;
            String saveFile = "save_file=" + SavegameStorage.EU4.getSavegameFile(eu4Entry).toString();
            String modsFolder = "mods_folder=" + GameInstallation.EU4.getUserPath().resolve("mod").toString();
            String gameFolder = "game_folder=" + GameInstallation.EU4.getPath().toString();
            String overwrite = "override=true";

            try {
                SavegameStorage.EU4.invalidateSavegameInfo(eu4Entry);
                var proc = new ProcessBuilder(
                        PdxuInstallation.getInstance().getEu4SaveEditorLocation()
                                .resolve("bin").resolve("Eu4SaveEditor.bat").toString(),
                        saveFile, modsFolder, gameFolder, overwrite)
                        .redirectErrorStream(true)
                        .start();
                proc.getInputStream().readAllBytes();
                SavegameActions.reloadSavegame(eu4Entry);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }).start();
    }
}
