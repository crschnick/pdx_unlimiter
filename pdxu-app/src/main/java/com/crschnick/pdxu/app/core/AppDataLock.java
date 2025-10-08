package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;

import org.apache.commons.io.FileUtils;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AppDataLock {

    private static FileChannel channel;
    private static FileLock lock;

    private static Path getLockFile() {
        return AppProperties.get().getDataDir().resolve("lock");
    }

    public static boolean hasLock() {
        return lock != null;
    }

    public static void init() {
        try {
            var file = getLockFile().toFile();
            FileUtils.forceMkdir(file.getParentFile());
            if (!Files.exists(file.toPath())) {
                try {
                    // It is possible that another instance creates the lock at almost the same time
                    // If it already exists, we lost the race
                    Files.createFile(file.toPath());
                } catch (FileAlreadyExistsException f) {
                    return;
                }
            }
            channel = new RandomAccessFile(file, "rw").getChannel();
            lock = channel.tryLock();
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).build().handle();
        }
    }

    public static void reset() {
        if (channel == null || lock == null) {
            return;
        }

        try {
            lock.release();
            channel.close();
            lock = null;
            channel = null;
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).build().handle();
        }
    }
}
