package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;

import java.util.List;

public class SavegameInfo {

    protected List<String> mods;
    protected List<String> dlcs;
    protected GameVersion version;

    public List<String> getMods() {
        return mods;
    }

    public List<String> getDlcs() {
        return dlcs;
    }

    public GameVersion getVersion() {
        return version;
    }
}
