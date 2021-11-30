package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.core.ErrorHandler;
import org.apache.commons.lang3.SystemUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class ThreadHelper {

    public static Thread create(String name, boolean daemon, Runnable r) {
        var t = new Thread(r);
        ErrorHandler.registerThread(t);
        t.setDaemon(daemon);
        t.setName(name);
        return t;
    }

    public static void browseDirectory(Path file) {
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + file.toString());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        } else {
            if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    return;
                }

                var t = new Thread(() -> {
                    try {
                        Desktop.getDesktop().open(file.getParent().toFile());
                    } catch (Exception e) {
                        ErrorHandler.handleException(e);
                    }
                });
                t.setDaemon(true);
                t.start();
                return;
            }

            var t = new Thread(() -> {
                try {
                    Desktop.getDesktop().browseFileDirectory(file.toFile());
                } catch (Exception e) {
                    ErrorHandler.handleException(e);
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    public static void browse(String uri) {
        var t = new Thread(() -> {
            try {
                Desktop.getDesktop().browse(URI.create(uri));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void open(Path p) {
        var t = new Thread(() -> {
            try {
                Desktop.getDesktop().open(p.toFile());
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
