package com.crschnick.pdx_unlimiter.app.savegame;


import com.crschnick.pdxu.model.SavegameInfo;

import java.time.Instant;
import java.util.UUID;

public class SavegameFolder<T, I extends SavegameInfo<T>> extends SavegameCollection<T, I> {

    public SavegameFolder(Instant lastPlayed, String name, UUID uuid) {
        super(lastPlayed, name, uuid);
    }
}
