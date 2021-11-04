package com.crschnick.pdxu.app.savegame;


import com.crschnick.pdxu.app.info.SavegameInfo;

import java.time.Instant;
import java.util.UUID;

public class SavegameFolder<T, I extends SavegameInfo<T>> extends SavegameCollection<T, I> {

    public SavegameFolder(Instant lastPlayed, String name, UUID uuid) {
        super(lastPlayed, name, uuid);
    }
}
