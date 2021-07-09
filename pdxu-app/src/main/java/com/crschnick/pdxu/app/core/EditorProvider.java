package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.model.SavegameInfo;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface EditorProvider {

    static EditorProvider get() {
        return ServiceLoader.load(EditorProvider.class).findFirst().orElseThrow();
    }

    void init();

    <T,I extends SavegameInfo<T>> void openSavegame(SavegameStorage<T,I> storage, SavegameEntry<T,I> entry);

    void openExternalFile();

    void openExternalDataFile(Path file);
}
