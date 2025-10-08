package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.savegame.FileImporter;

import java.util.ArrayList;
import java.util.List;

public class AppOpenArguments {

    private static final List<String> bufferedArguments = new ArrayList<>();

    public static synchronized void init() {
        handleImpl(AppProperties.get().getArguments().getOpenArgs());
        handleImpl(bufferedArguments);
        bufferedArguments.clear();
    }

    public static synchronized void handle(List<String> arguments) {
        if (AppOperationMode.isInShutdown()) {
            return;
        }

        if (AppOperationMode.isInStartup()) {
            TrackEvent.withDebug("Buffering open arguments").elements(arguments).handle();
            bufferedArguments.addAll(arguments);
            return;
        }

        handleImpl(arguments);
    }

    private static synchronized void handleImpl(List<String> arguments) {
        if (arguments.isEmpty()) {
            return;
        }

        TrackEvent.withDebug("Handling arguments").elements(arguments).handle();
        FileImporter.onArgumentsPass(arguments);
    }
}
