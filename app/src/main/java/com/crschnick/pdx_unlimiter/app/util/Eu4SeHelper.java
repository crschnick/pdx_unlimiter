package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import static com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper.createAlert;

public class Eu4SeHelper {

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

        return SavegameStorage.EU4 != null && SavegameStorage.EU4.contains(entry);
    }

    public static void open(SavegameEntry<?, ?> entry) {
        if (!PdxuInstallation.getInstance().isEu4SaveEditorInstalled()) {
            showUsageDialog();
            return;
        }

        ThreadHelper.create("eu4se", true, () -> {
            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) entry;
            String saveFile = "save_file=" + SavegameStorage.EU4.getSavegameFile(eu4Entry).toString();
            String modsFolder = "mods_folder=" + GameInstallation.EU4.getUserPath().resolve("mod").toString();
            String gameFolder = "game_folder=" + GameInstallation.EU4.getPath().toString();

            try {
                SavegameStorage.EU4.invalidateSavegameInfo(eu4Entry);
                var proc = new ProcessBuilder(
                        PdxuInstallation.getInstance().getEu4SaveEditorLocation()
                                .resolve("bin").resolve("Eu4SaveEditor.bat").toString(),
                        saveFile, modsFolder, gameFolder).start();
                proc.getInputStream().readAllBytes();
                SavegameStorage.EU4.reloadSavegameAsync(eu4Entry);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }).start();
    }
}
