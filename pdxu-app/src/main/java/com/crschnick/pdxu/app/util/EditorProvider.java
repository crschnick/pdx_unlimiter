package com.crschnick.pdxu.app.util;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface EditorProvider {

    static EditorProvider get() {
        return ServiceLoader.load(EditorProvider.class).findFirst().orElseThrow();
    }

    void init();

    <T,I extends SavegameInfo<T>> void openSavegame(SavegameStorage<T,I> storage, SavegameEntry<T,I> entry);

    void browseExternalFile(Game g);

    void openExternalFileIfNoSavegame(Path file);

    String getDefaultEditor();
}
