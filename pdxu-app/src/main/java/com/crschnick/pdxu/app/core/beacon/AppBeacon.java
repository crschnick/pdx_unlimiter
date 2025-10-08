package com.crschnick.pdxu.app.core.beacon;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.util.OsType;

import java.io.IOException;

public abstract class AppBeacon {

    private static AppBeacon INSTANCE;

    public abstract boolean isExistingBeaconRunning();

    public abstract void sendRequest(AppBeaconMessage message);

    public static void init() {
        try {
            INSTANCE = OsType.ofLocal() == OsType.WINDOWS ? new AppNamedPipeBeacon() : new AppUnixSocketBeacon();
            INSTANCE.start();
        } catch (Exception ex) {
            // Not terminal!
            // We can still continue without the running server
            ErrorEventFactory.fromThrowable("Unable to start beacon", ex).handle();
        }
    }

    public static void reset() {
        if (INSTANCE != null) {
            INSTANCE.stop();
            INSTANCE = null;
        }
    }

    public static AppBeacon get() {
        return INSTANCE;
    }

    protected abstract void stop();

    protected abstract void start() throws IOException;
}
