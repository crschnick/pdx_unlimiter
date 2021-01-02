package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.gui.GuiErrorReporter;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
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

    public static <T, I extends SavegameInfo<T>> void uploadSavegame(SavegameCache<T, I> cache, GameCampaignEntry<T, I> entry) {
        if (Settings.getInstance().getSkanderbegApiKey().isEmpty()) {
            GuiErrorReporter.showSimpleErrorMessage("Missing skanderbeg.pm API key. " +
                    "To use this functionality, set it in the settings menu.");
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            try {
                byte[] body = Files.readAllBytes(cache.getSavegameFile(entry));
                String saveId = uploadContent(body, cache.getFileName(entry));
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
                        Settings.getInstance().getSkanderbegApiKey().get() + "&scope=uploadSaveFile"))
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
