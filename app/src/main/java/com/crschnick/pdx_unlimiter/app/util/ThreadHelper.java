package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;

import java.awt.*;
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
