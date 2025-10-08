package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.ThreadHelper;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class AppFileWatcher {

    private static AppFileWatcher INSTANCE;

    private final Set<WatchedDirectory> watchedDirectories = new CopyOnWriteArraySet<>();
    private WatchService watchService;
    private Thread watcherThread;
    private boolean active;

    public static AppFileWatcher getInstance() {
        return INSTANCE;
    }

    public static void init() {
        INSTANCE = new AppFileWatcher();
        INSTANCE.startWatcher();
    }

    public static void reset() {
        INSTANCE.stopWatcher();
        INSTANCE = null;
    }

    public void startWatchersInDirectories(List<Path> dirs, BiConsumer<Path, WatchEvent.Kind<Path>> listener) {
        // Check in case initialization failed
        if (watchService == null) {
            return;
        }

        dirs.forEach(d -> watchedDirectories.add(new WatchedDirectory(d, listener)));
    }

    private void startWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(
                            "Unable to initialize file watcher. Watching and updating files in the file browser will be unavailable.",
                            e)
                    .expected()
                    .handle();
            return;
        }

        active = true;
        watcherThread = ThreadHelper.createPlatformThread("file watcher", true, () -> {
            while (active) {
                WatchKey key;
                try {
                    key = AppFileWatcher.this.watchService.poll(10, TimeUnit.MILLISECONDS);
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
                    ErrorEventFactory.fromThrowable(ex).handle();
                }

                // Don't sleep, since polling the directories always sleeps for some ms
            }
        });
        watcherThread.start();
    }

    private void stopWatcher() {
        // Check in case initialization failed
        if (watchService == null) {
            return;
        }

        active = false;
        watchedDirectories.clear();

        try {
            watchService.close();
        } catch (IOException e) {
            ErrorEventFactory.fromThrowable(e).omit().handle();
        }

        try {
            watcherThread.join();
        } catch (InterruptedException e) {
            ErrorEventFactory.fromThrowable(e).omit().handle();
        }
    }

    private class WatchedDirectory {
        private final BiConsumer<Path, WatchEvent.Kind<Path>> listener;

        @Getter
        private final Path baseDir;

        private WatchedDirectory(Path dir, BiConsumer<Path, WatchEvent.Kind<Path>> listener) {
            this.baseDir = dir;
            this.listener = listener;
            createRecursiveWatchers(dir);
            TrackEvent.withTrace("Added watched directory").tag("location", dir).handle();
        }

        private void createRecursiveWatchers(Path dir) {
            if (!Files.isDirectory(dir)) {
                return;
            }

            try {
                dir.register(AppFileWatcher.this.watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                Files.list(dir).filter(Files::isDirectory).forEach(this::createRecursiveWatchers);
            } catch (IOException e) {
                ErrorEventFactory.fromThrowable(e).omit().handle();
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

            // Check for outdated info
            if (ev.kind() == StandardWatchEventKinds.ENTRY_CREATE || ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                var ex = Files.exists(file);
                if (!ex) {
                    return;
                }
            }

            // Add new watcher for directory
            if (ev.kind().equals(ENTRY_CREATE) && Files.isDirectory(file)) {
                try {
                    file.register(AppFileWatcher.this.watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                } catch (IOException e) {
                    ErrorEventFactory.fromThrowable(e).omit().handle();
                }
            }

            // Handle event
            TrackEvent.withTrace("Watch event")
                    .tag("baseDir", baseDir)
                    .tag("file", baseDir.relativize(file))
                    .tag("kind", event.kind().name())
                    .handle();
            listener.accept(file, ev.kind());
        }
    }
}
