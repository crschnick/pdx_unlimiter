package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.dialog.GuiErrorReporter;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SkanderbegHelper {

    public static <T, I extends SavegameInfo<T>> void uploadSavegame(SavegameEntry<T, I> entry) {
        if (Settings.getInstance().skanderbegApiKey.getValue() != null) {
            GuiErrorReporter.showSimpleErrorMessage("Missing skanderbeg.pm API key. " +
                    "To use this functionality, set it in the settings menu.");
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                byte[] body = Files.readAllBytes(SavegameStorage.ALL.get(Game.EU4).getSavegameFile(entry));
                if (entry.getInfo().getData().isIronman()) {
                    body = RakalyHelper.toPlaintext(SavegameStorage.ALL.get(Game.EU4).getSavegameFile(entry));
                }

                String saveId = uploadContent(body, SavegameStorage.ALL.get(Game.EU4)
                        .getCompatibleName(entry, true));
                ThreadHelper.browse("https://skanderbeg.pm/browse.php?id=" + saveId);
            } catch (Exception e) {
                GuiErrorReporter.showSimpleErrorMessage(e.getMessage());
            }
        }, true);
    }

    private static String uploadContent(byte[] content, String fileName) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        String prefix = "--2a8ae6ad-f4ad-4d9a-a92c-6d217011fe0f\nContent-Disposition: form-data; name=\"file\"; filename=\"" +
                fileName + "\"\n\n";
        String suffix = "\n--2a8ae6ad-f4ad-4d9a-a92c-6d217011fe0f--";
        byte[] body = new byte[content.length + prefix.length() + suffix.length()];
        System.arraycopy(prefix.getBytes(StandardCharsets.UTF_8), 0, body, 0, prefix.length());
        System.arraycopy(content, 0, body, prefix.length(), content.length);
        System.arraycopy(suffix.getBytes(StandardCharsets.UTF_8), 0, body, content.length + prefix.length(), suffix.length());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://skanderbeg.pm/api.php?key=" +
                        Settings.getInstance().skanderbegApiKey.getValue() + "&scope=uploadSaveFile"))
                .header("Content-Type", "multipart/form-data; boundary=2a8ae6ad-f4ad-4d9a-a92c-6d217011fe0f")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int responseCode = response.statusCode();
        if (responseCode != 200) {
            String msg = "Skanderbeg.pm returned http " + responseCode;
            throw new IOException(msg);
        } else {
            ObjectMapper o = new ObjectMapper();
            JsonNode node = o.readTree(response.body());
            boolean success = node.required("success").booleanValue();
            if (!success) {
                throw new IOException("Skanderbeg.pm could not parse savegame");
            }
            return node.required("hash").textValue();
        }
    }
}
