package com.crschnick.pdxu.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

public class MemoryHelper {

    private static final Logger logger = LoggerFactory.getLogger(MemoryHelper.class);

    public static void forceGC() {
        var used = Runtime.getRuntime().totalMemory();
        logger.debug("Used memory: " + used / 1024 + "kB");
        logger.debug("Running gc ...");

        Object obj = new Object();
        WeakReference<?> ref = new WeakReference<>(obj);
        obj = null;
        while (ref.get() != null) {
            System.gc();
            ThreadHelper.sleep(20);
        }

        var usedAfter = Runtime.getRuntime().totalMemory();
        logger.debug("Used memory after gc: " + usedAfter / 1024 + "kB");
    }
}
