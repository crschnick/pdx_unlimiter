package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RakalyWebHelper {

    private static byte[] writeJsBlob(byte[] bytes) throws IOException {
        List<byte[]> byteMap = new ArrayList<>(256);
        for (int i = 0; i < 256; i++) {
            byteMap.add(String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }

        try (var out = new ByteArrayOutputStream(20000000)) {
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
            return out.toByteArray();
        }
    }

    static class MyHandler implements HttpHandler {

        private SavegameEntry<?,?> entry;
        private SavegameStorage<?,?> storage;

        public void handle(HttpExchange t) throws IOException {
            byte [] response;
            if (t.getRequestURI().toString().equals("/pdxu_rakaly_blob.js")) {
                response = writeJsBlob(Files.readAllBytes(storage.getSavegameFile(entry)));
            } else {
                var src = PdxuInstallation.getInstance().getResourceDir().resolve("web").resolve("rakaly.html");
                response = Files.readAllBytes(src);
            }

            t.sendResponseHeaders(200, response.length);
            OutputStream os = t.getResponseBody();
            os.write(response);
            os.close();
        }

        public void setEntry(SavegameEntry<?,?> e) {
            storage = SavegameContext.getContext(e).getStorage();
            entry = e;
        }
    }

    private static MyHandler handler;
    private static HttpServer server;

    private static void startWebServer() throws IOException {
        if (server != null) {
            return;
        }

        handler = new MyHandler();
        server = HttpServer.create(new InetSocketAddress(8135), 0);
        server.createContext("/", handler);
        server.setExecutor(null);
        server.start();
    }

    public static void uploadSavegame(SavegameEntry<?, ?> entry) {
        TaskExecutor.getInstance().submitTask(() -> {
            try {
                startWebServer();
                handler.setEntry(entry);
                ThreadHelper.browse("http://localhost:8135");
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }, true);
    }

    public static void shutdownServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            handler = null;
        }
    }
}
