package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatcherHelper {

    private static Map<WatchService,Path> createRecursiveWatchers(Path dir) {
        Map<WatchService,Path> watchers = new HashMap<>();
        try {
            var w = FileSystems.getDefault().newWatchService();
            dir.register(w, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
            watchers.put(w, dir);
            Files.list(dir).filter(Files::isDirectory).forEach(d -> watchers.putAll(createRecursiveWatchers(d)));
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
        return watchers;
    }

    public static void startWatchersInDirectories(
            String game,
            List<Path> dirs,
            Consumer<Path> listener) {
        Map<WatchService,Path> watchers = new HashMap<>();
        dirs.forEach(d -> watchers.putAll(createRecursiveWatchers(d)));
        startWatcher(game + " savegame watcher", watchers, listener);
    }

    private static void startWatcher(String name, Map<WatchService,Path> watchers, Consumer<Path> listener) {
        Thread t = new Thread(() -> {
            while (true) {
                for (var entry : new HashMap<>(watchers).entrySet()) {
                    WatchKey key;
                    try {
                        key = entry.getKey().poll(500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        return;
                    }

                    if (!PdxuApp.getApp().isRunning()) {
                        return;
                    }

                    if (key == null) {
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path file = entry.getValue().resolve(ev.context());
                        while (true) {
                            try {
                                if (!Files.exists(file)) {
                                    break;
                                }

                                FileLock lock = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)
                                        .lock(0, Long.MAX_VALUE, false);
                                lock.release();
                                break;
                            } catch (Exception e) {
                            }
                        }

                        if (ev.kind().equals(ENTRY_CREATE) && Files.isDirectory(file)) {
                            WatchService watcher = null;
                            try {
                                watcher = FileSystems.getDefault().newWatchService();
                                file.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            watchers.put(watcher, file);
                        }

                        listener.accept(file);
                    }

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        });
        t.setName(name);
        t.start();
    }
}
