package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class SavegameWatcher {

    private boolean running = false;

    public void startInDirectory(String directory) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(directory);
        dir.register(watcher, ENTRY_CREATE);

        running = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    WatchKey key;
                    try {
                        // wait for a key to be available
                        key = watcher.take();
                    } catch (InterruptedException ex) {
                        return;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        // get event type
                        WatchEvent.Kind<?> kind = event.kind();

                        // get file name
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path fileName = ev.context();

                        System.out.println(kind.name() + ": " + fileName);

                        if (kind == OVERFLOW) {
                            continue;
                        } else if (kind == ENTRY_CREATE) {

                            // process create event

                        }
                    }

                    // IMPORTANT: The key must be reset after processed
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
    }
}
