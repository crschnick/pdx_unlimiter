package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import static com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper.createAlert;

public class RakalyWebHelper {

    public static void showUsageDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);

        var open = new ButtonType("Visit Rakaly", ButtonBar.ButtonData.APPLY);
        alert.getButtonTypes().add(open);
        Button val = (Button) alert.getDialogPane().lookupButton(open);
        val.setOnAction(e -> {
            ThreadHelper.browse("https://rakaly.com/eu4");
        });

        alert.setAlertType(Alert.AlertType.INFORMATION);
        alert.setTitle("Rakaly.com upload functionality");
        alert.setHeaderText("""
                Rakaly.com is a website to analyze and share your eu4 achievements and compete against other players.
                """);
        alert.setContentText("""
                You can upload saves with this button by signing into Rakaly.com through Steam and then setting your
                Rakaly.com User ID and API key in the settings menu.
                """);
        alert.showAndWait();
    }

    public static void uploadSavegame(SavegameEntry<?, ?> entry) {
        if (Settings.getInstance().rakalyApiKey.getValue() == null ||
                Settings.getInstance().rakalyUserId.getValue() == null) {
            showUsageDialog();
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                var proc = new ProcessBuilder(
                        PdxuInstallation.getInstance().getRakalyExecutable().toString(),
                        "upload",
                        "--user", Settings.getInstance().rakalyUserId.getValue(),
                        "--api-key", Settings.getInstance().rakalyApiKey.getValue(),
                        SavegameStorage.ALL.get(Game.EU4)
                                .getSavegameFile((SavegameEntry<Eu4Tag, Eu4SavegameInfo>) entry).toString())
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start();
                var out = new String(proc.getInputStream().readAllBytes());
                out.lines().findFirst().ifPresent(l -> {
                    ThreadHelper.browse("https://rakaly.com/eu4/saves/" + l);
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }
}
