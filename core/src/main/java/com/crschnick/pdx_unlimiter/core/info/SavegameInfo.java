package com.crschnick.pdx_unlimiter.core.info;

import com.crschnick.pdx_unlimiter.core.info.GameDate;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class SavegameInfo<T> {

    protected UUID campaignHeuristic;
    protected List<String> mods;
    protected List<String> dlcs;
    protected GameVersion version;
    protected boolean ironman;
    protected UUID campaignUuid;
    protected GameDate date;
    protected boolean binary;

    public List<String> getMods() {
        return mods;
    }

    public List<String> getDlcs() {
        return dlcs;
    }

    public abstract T getTag();

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

    public abstract Set<T> getAllTags();

    public boolean isBinary() {
        return binary;
    }

    public UUID getCampaignHeuristic() {
        return campaignHeuristic;
    }
}
