package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatchManager {

    private static FileWatchManager INSTANCE = new FileWatchManager();
    private Set<WatchedDirectory> watchedDirectories = new CopyOnWriteArraySet<>();

    public static FileWatchManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        INSTANCE.startWatcher();
    }

    public static void reset() {
        INSTANCE.watchedDirectories.forEach(WatchedDirectory::stop);
        INSTANCE.watchedDirectories.clear();
    }

    public void startWatchersInDirectories(
            List<Path> dirs,
            BiConsumer<Path, WatchEvent.Kind<Path>> listener) {
        dirs.forEach(d -> watchedDirectories.add(WatchedDirectory.create(d, listener)));
    }

    private void startWatcher() {
        TaskExecutor.getInstance().submitLoop(() -> {
            for (var wd : new HashSet<>(watchedDirectories)) {
                wd.update();
            }
        });
    }

    private static class WatchedDirectory {
        private Path directory;
        private BiConsumer<Path, WatchEvent.Kind<Path>> listener;
        private Map<Path, WatchService> watchers = new ConcurrentHashMap<>();

        public static WatchedDirectory create(Path dir, BiConsumer<Path, WatchEvent.Kind<Path>> listener) {
            var w = new WatchedDirectory();
            w.directory = dir;
            w.listener = listener;
            w.watchers.putAll(createRecursiveWatchers(dir));
            return w;
        }

        private static Map<Path, WatchService> createRecursiveWatchers(Path dir) {
            Map<Path, WatchService> watchers = new HashMap<>();
            if (!Files.isDirectory(dir)) {
                return watchers;
            }

            try {
                var w = FileSystems.getDefault().newWatchService();
                dir.register(w, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                watchers.put(dir, w);
                Files.list(dir).filter(Files::isDirectory).forEach(d -> watchers.putAll(createRecursiveWatchers(d)));
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
            return watchers;
        }

        public void update() {
            for (var entry : watchers.entrySet()) {
                WatchKey key;
                try {
                    key = entry.getValue().poll(100, TimeUnit.MILLISECONDS);
                } catch (Exception ex) {
                    continue;
                }

                if (key == null) {
                    continue;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    handleWatchEvent(entry.getKey(), event);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        }

        private void handleWatchEvent(Path path, WatchEvent<?> event) {
            @SuppressWarnings("unchecked")
            WatchEvent<Path> ev = (WatchEvent<Path>) event;
            Path file = path.resolve(ev.context());

            // Only wait for write access for new files
            if (Files.exists(file) && !Files.isDirectory(file)) {
                while (true) {
                    try {
                        // Wait for file to finish writing
                        FileLock lock = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE)
                                .lock(0, Long.MAX_VALUE, false);
                        lock.release();
                        break;
                    } catch (Exception e) {
                        ThreadHelper.sleep(100);
                    }
                }
            }

            // Add new watcher for directory
            if (ev.kind().equals(ENTRY_CREATE) && Files.isDirectory(file)) {
                WatchService watcher = null;
                try {
                    watcher = FileSystems.getDefault().newWatchService();
                    file.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                watchers.put(file, watcher);
            }

            // Handle event
            listener.accept(file, ev.kind());
        }

        public void stop() {
            for (var e : watchers.entrySet()) {
                try {
                    e.getValue().close();
                } catch (IOException ioException) {
                    ErrorHandler.handleException(ioException);
                }
            }
        }

        public Path getDirectory() {
            return directory;
        }
    }
}
