package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.Settings;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Optional;

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

    public static <T, I extends SavegameInfo<T>> void uploadSavegame(SavegameStorage<T, I> cache, SavegameEntry<T, I> entry) {
        if (Settings.getInstance().getRakalyApiKey().isEmpty() || Settings.getInstance().getRakalyUserId().isEmpty()) {
            showUsageDialog();
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                byte[] body = Files.readAllBytes(cache.getPath(entry).resolve("savegame.eu4"));
                String saveId = executePost(cache.getFileName(entry), new URL("https://rakaly.com/api/saves"), body);
                ThreadHelper.browse("https://rakaly.com/eu4/saves/" + saveId);
            } catch (Exception e) {
                GuiErrorReporter.showSimpleErrorMessage(e.getMessage());
            }
        }, true);
    }

    private static String executePost(String sgName, URL targetURL, byte[] data) throws IOException {
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) targetURL.openConnection();
            connection.setRequestMethod("POST");
            connection.addRequestProperty("User-Agent", "https://github.com/crschnick/pdx_unlimiter");
            connection.addRequestProperty("Accept", "application/json");
            connection.addRequestProperty("Content-Length", String.valueOf(data.length));
            connection.addRequestProperty("rakaly-filename", sgName);

            String encoding = Base64.getEncoder().encodeToString(
                    (Settings.getInstance().getRakalyUserId().get() + ":" + Settings.getInstance().getRakalyApiKey().get())
                            .getBytes(StandardCharsets.UTF_8));
            connection.setRequestProperty("Authorization", "Basic " + encoding);
            connection.setRequestProperty("Content-Type", "application/zip");

            connection.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.write(data);

            int responseCode = connection.getResponseCode();
            InputStream is;
            if (responseCode != 200) {
                is = connection.getErrorStream();
            } else {
                is = connection.getInputStream();
            }

            ObjectMapper o = new ObjectMapper();
            JsonNode node = o.readTree(is.readAllBytes());

            if (responseCode != 200) {
                String msg = Optional.of(node.get("msg"))
                        .map(JsonNode::textValue).orElse("Rakaly returned http " + responseCode);
                throw new IOException(msg);
            } else {
                String saveId = node.required("save_id").textValue();
                return saveId;
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }

    public static <T, I extends SavegameInfo<T>> void analyzeEntry(SavegameStorage<T, I> cache, SavegameEntry<T, I> entry) {
        TaskExecutor.getInstance().submitTask(() -> {
            try {
                ThreadHelper.browse("https://rakaly.com/eu4/analyze");
            } catch (Exception e) {
                GuiErrorReporter.showSimpleErrorMessage(e.getMessage());
            }
        }, true);
    }
}
