package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;

import static com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper.createAlert;

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
            Hyperlinks.open(Hyperlinks.EU4_SE_MAIN_PAGE);
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

    public static boolean isSupported() {
        if (PdxuInstallation.getInstance().isStandalone()) {
            return false;
        }

        return SystemUtils.IS_OS_WINDOWS;
    }

    public static boolean shouldShowButton(SavegameEntry<?, ?> entry, SavegameInfo<?> info) {
        if (!isSupported()) {
            return false;
        }

        if (info.isBinary()) {
            return false;
        }

        if (SavegameStorage.ALL.get(Game.EU4) != null && SavegameStorage.ALL.get(Game.EU4).contains(entry)) {
            Eu4SavegameInfo eu4i = (Eu4SavegameInfo) info;
            return !eu4i.getData().eu4().isRandomNewWorld();
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
            var modDir = GameInstallation.ALL.get(Game.EU4).getUserDir().resolve("mod");
            if (!Files.exists(modDir)) {
                try {
                    Files.createDirectory(modDir);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                    return;
                }
            }

            @SuppressWarnings("unchecked")
            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) entry;
            String saveFile = "save_file=" + SavegameStorage.ALL.get(Game.EU4).getSavegameFile(eu4Entry).toString();
            String modsFolder = "mods_folder=" + GameInstallation.ALL.get(Game.EU4).getUserDir().resolve("mod").toString();
            String gameFolder = "game_folder=" + GameInstallation.ALL.get(Game.EU4).getInstallDir().toString();
            String overwrite = "override=true";

            try {
                SavegameStorage.<Eu4Tag, Eu4SavegameInfo>get(Game.EU4).invalidateSavegameInfo(eu4Entry);
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
