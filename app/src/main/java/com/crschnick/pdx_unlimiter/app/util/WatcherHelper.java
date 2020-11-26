package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.PdxuApp;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatcherHelper {

    public static void startWatcherInDirectory(String name, Path dir, Consumer<Path> listener, WatchEvent.Kind<?>... types) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        dir.register(watcher, types);

        Thread t = new Thread(() -> {
            while (true) {
                WatchKey key;
                try {
                    key = watcher.poll(500, TimeUnit.MILLISECONDS);
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
                    Path fileName = ev.context();
                    while (true) {
                        try {
                            if (!Files.exists(fileName)) {
                                break;
                            }

                            FileLock lock = FileChannel.open(dir.resolve(fileName), StandardOpenOption.READ, StandardOpenOption.WRITE)
                                    .lock(0, Long.MAX_VALUE, false);
                            lock.release();
                            break;
                        } catch (Exception e) {
                        }
                    }

                    listener.accept(dir.resolve(fileName));
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        });
        t.setName(name);
        t.start();
    }
}
