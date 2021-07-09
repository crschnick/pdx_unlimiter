package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.commons.io.FileUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper.createAlert;

public class RakalyWebHelper {

    private static void writeJsBlob(byte[] bytes) throws IOException {
        List<byte[]> byteMap = new ArrayList<>(256);
        for (int i = 0; i < 256; i++) {
            byteMap.add(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }

        var file = FileUtils.getTempDirectory().toPath().resolve("pdxu_rakaly_blob.js");
        try (var out = new BufferedOutputStream(Files.newOutputStream(file), 10000000)) {
            out.write("""
                    function createBuffer() {
                        var buffer = new Uint8Array([
                            """.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < bytes.length; i++) {
                out.write(byteMap.get(Byte.toUnsignedInt(bytes[i])));
                out.write(',');
                if (i % 64 == 0) {
                    out.write('\n');
                }
            }

            out.write("""
                        ]);
                        return buffer;
                    }
                    """.getBytes(StandardCharsets.UTF_8));
        }
    }

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
                var bytes = Files.readAllBytes(
                        SavegameContext.getContext(entry).getStorage().getSavegameFile(entry));
                writeJsBlob(bytes);
                var toOpen = Files.copy(PdxuInstallation.getInstance().getResourceDir().resolve("res").resolve("rakaly.html"),
                        FileUtils.getTempDirectory().toPath().resolve("pdxu_rakaly.html"), StandardCopyOption.REPLACE_EXISTING);
                ThreadHelper.open(toOpen);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }
}
