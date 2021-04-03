package com.crschnick.pdx_unlimiter.core.info;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class SavegameInfo<T> {

    protected UUID campaignHeuristic;
    protected List<String> mods;
    protected List<String> dlcs;
    protected GameVersion version;
    protected boolean ironman;
    protected GameDate date;
    protected boolean binary;
    protected boolean observer;
    protected boolean multiplayer;

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

    public GameDate getDate() {
        return date;
    }

    public GameVersion getVersion() {
        return version;
    }

    public abstract List<T> getAllTags();

    public boolean isBinary() {
        return binary;
    }

    public UUID getCampaignHeuristic() {
        return campaignHeuristic;
    }

    public boolean hasOnePlayerTag() {
        return getTag() != null;
    }

    public boolean isObserver() {
        return observer;
    }

    public boolean isMultiplayer() {
        return multiplayer;
    }
}
