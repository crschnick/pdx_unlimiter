package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;

public class ThreadHelper {

    public static Thread create(String name, boolean daemon, Runnable r) {
        var t = new Thread(r);
        ErrorHandler.registerThread(t);
        t.setDaemon(daemon);
        t.setName(name);
        return t;
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
