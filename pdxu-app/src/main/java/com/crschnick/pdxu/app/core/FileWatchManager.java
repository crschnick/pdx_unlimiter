package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.util.ThreadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatchManager {

    private static final FileWatchManager INSTANCE = new FileWatchManager();
    private static final Logger logger = LoggerFactory.getLogger(FileWatchManager.class);

    private final Set<WatchedDirectory> watchedDirectories = new CopyOnWriteArraySet<>();
    private WatchService watchService;
    private Thread watcherThread;
    private boolean active;

    public static FileWatchManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        INSTANCE.startWatcher();
    }

    public static void reset() {
        INSTANCE.stopWatcher();
    }

    public void startWatchersInDirectories(
            List<Path> dirs,
            BiConsumer<Path, WatchEvent.Kind<Path>> listener) {
        dirs.forEach(d -> watchedDirectories.add(new WatchedDirectory(d, listener)));
    }

    private void startWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return;
        }

        active = true;
        watcherThread = ThreadHelper.create("file watcher", true, () -> {
            while (active) {
                WatchKey key;
                try {
                    key = FileWatchManager.this.watchService.poll(10, TimeUnit.MILLISECONDS);
                    if (key == null) {
                        continue;
                    }

                    for (var wd : new HashSet<>(watchedDirectories)) {
                        wd.update(key);
                    }
                } catch (ClosedWatchServiceException ex) {
                    // Exit loop if watch service is closed
                    break;
                } catch (Exception ex) {
                    // Catch all other exceptions to not terminate this thread if an error occurs!
                    ErrorHandler.handleException(ex);
                }

                // Don't sleep, since polling the directories always sleeps for some ms
            }
        });
        watcherThread.start();
    }

    private void stopWatcher() {
        active = false;
        watchedDirectories.clear();

        try {
            watchService.close();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

        try {
            watcherThread.join();
        } catch (InterruptedException e) {
            ErrorHandler.handleException(e);
        }
    }

    private class WatchedDirectory {
        private final BiConsumer<Path, WatchEvent.Kind<Path>> listener;
        private final Path baseDir;

        private WatchedDirectory(Path dir, BiConsumer<Path, WatchEvent.Kind<Path>> listener) {
            this.baseDir = dir;
            this.listener = listener;
            createRecursiveWatchers(dir);
            logger.trace("Created watched directory for " + dir);
        }

        private void createRecursiveWatchers(Path dir) {
            if (!Files.isDirectory(dir) || !Files.isReadable(dir)) {
                return;
            }

            try {
                dir.register(FileWatchManager.this.watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                try (var s = Files.list(dir)) {
                    s.filter(Files::isDirectory).forEach(this::createRecursiveWatchers);
                }
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }

        public void update(WatchKey key) {
            Path dir = (Path) key.watchable();
            if (!dir.startsWith(getBaseDir())) {
                return;
            }


            while (true) {
                var baseDir = key.watchable();

                var events = key.pollEvents();
                if (events.size() == 0) {
                    break;
                }

                for (WatchEvent<?> event : events) {
                    handleWatchEvent((Path) baseDir, event);
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

            // Context may be null for whatever reason
            if (ev.context() == null) {
                return;
            }
            Path file = path.resolve(ev.context());

            // Add new watcher for directory
            if (ev.kind().equals(ENTRY_CREATE) && Files.isDirectory(file)) {
                try {
                    file.register(FileWatchManager.this.watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            }

            // Handle event
            logger.trace("WatchEvent " + event.kind().name() + " of file " +
                    baseDir.relativize(file) + " in dir " + baseDir);
            listener.accept(file, ev.kind());
        }

        public Path getBaseDir() {
            return baseDir;
        }
    }
}
