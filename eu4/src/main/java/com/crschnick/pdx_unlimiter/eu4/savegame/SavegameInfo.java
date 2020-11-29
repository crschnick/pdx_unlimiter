package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.data.GameDate;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;

import java.util.List;
import java.util.UUID;

public class SavegameInfo<T> {

    protected List<String> mods;
    protected List<String> dlcs;
    protected GameVersion version;
    protected boolean ironman;
    protected UUID campaignUuid;
    protected GameDate date;
    protected T tag;

    public List<String> getMods() {
        return mods;
    }

    public List<String> getDlcs() {
        return dlcs;
    }

    public T getTag() {
        return tag;
    }

    public boolean isIronman() {
        return ironman;
    }

    public UUID getCampaignUuid() {
        return campaignUuid;
    }

    public GameDate getDate() {
        return date;
    }

    public GameVersion getVersion() {
        return version;
    }
}
