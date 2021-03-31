package com.crschnick.pdx_unlimiter.app.savegame;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface EditorProvider {

    static void openFile(Path file) {
        ServiceLoader<EditorProvider> loader = ServiceLoader
                .load(EditorProvider.class);
        loader.findFirst().ifPresent(p -> p.open(file));
    }

    static void openEntry(SavegameEntry<?,?> entry) {
        ServiceLoader<EditorProvider> loader = ServiceLoader
                .load(EditorProvider.class);
        loader.findFirst().ifPresent(p -> p.open(entry));
    }

    void open(Path file);

    void open(SavegameEntry<?,?> entry);
}
