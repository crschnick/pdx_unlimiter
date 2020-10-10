package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class SavegameWatcher {

    public static void startInDirectory(Path directory, Consumer<Path> consumer) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        directory.register(watcher, ENTRY_CREATE);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    WatchKey key;
                    try {
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        System.out.println(kind.name() + ": " + fileName);

                        if (kind == OVERFLOW) {
                            continue;
                        } else if (kind == ENTRY_CREATE) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            consumer.accept(directory.resolve(fileName));
                        }
                    }

                    // IMPORTANT: The key must be reset after processed
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
