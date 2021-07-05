package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import static com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper.createAlert;

public class RakalyWebHelper {

    public static void showUsageDialog() {
        Alert alert = createAlert();
        alert.setAlertType(Alert.AlertType.INFORMATION);

        var open = new ButtonType("Visit Rakaly", ButtonBar.ButtonData.APPLY);
        alert.getButtonTypes().add(open);
        Button val = (Button) alert.getDialogPane().lookupButton(open);
        val.setOnAction(e -> {
            Hyperlinks.open(Hyperlinks.RAKALY_MAIN_PAGE);
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
                var copy = SavegameActions.exportToTemp(entry, false).orElseThrow();
                var proc = new ProcessBuilder(
                        PdxuInstallation.getInstance().getRakalyExecutable().toString(),
                        "upload",
                        "--user", Settings.getInstance().rakalyUserId.getValue(),
                        "--api-key", Settings.getInstance().rakalyApiKey.getValue(),
                        copy.toString())
                        .redirectErrorStream(true)
                        .start();
                var out = new String(proc.getInputStream().readAllBytes());
                proc.waitFor();
                if (proc.exitValue() != 0) {
                    GuiErrorReporter.showSimpleErrorMessage(out);
                } else {
                    out.lines().findFirst().ifPresent(l -> {
                        ThreadHelper.browse("https://rakaly.com/eu4/saves/" + l);
                    });
                }
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }
}
