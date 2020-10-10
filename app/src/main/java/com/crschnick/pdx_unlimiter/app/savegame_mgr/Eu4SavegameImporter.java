package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import javafx.beans.property.BooleanProperty;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class Eu4SavegameImporter {

    public static void importAllSavegames(BooleanProperty running) {
        new Thread(() -> {
            Eu4SavegameImporter.importAllSavegames(Installation.EU4.get().getSaveDirectory(), (p) -> {
                try {
                    SavegameCache.EU4_CACHE.importSavegame(p, Eu4Savegame.fromFile(p));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, running);
        }).start();

    }

    private static void importAllSavegames(Path directory, Consumer<Path> consumer, BooleanProperty running) {
        for (File f : directory.toFile().listFiles()) {
            if (!running.get()) {
                break;
            }
            consumer.accept(f.toPath());
        }
    }

    public static void startWatcher(Path directory, BooleanProperty running) {
        try {
            Eu4SavegameImporter.startInDirectory(directory.resolve("save games"), (p) -> {
                try {
                    SavegameCache.EU4_CACHE.importSavegame(p, Eu4Savegame.fromFile(p));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, running);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startInDirectory(Path directory, Consumer<Path> consumer, BooleanProperty running) throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        directory.register(watcher, ENTRY_CREATE);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running.get()) {
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
                                FileChannel c = FileChannel.open(directory.resolve(fileName), StandardOpenOption.WRITE);
                                c.lock().release();
                                consumer.accept(directory.resolve(fileName));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
        t.start();
    }
}
