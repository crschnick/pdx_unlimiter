package com.crschnick.pdxu.model;

import java.util.List;
import java.util.UUID;

public abstract class SavegameInfo<T> {

    protected UUID campaignHeuristic;
    protected List<String> mods;
    protected List<String> dlcs;
    protected boolean ironman;
    protected GameDate date;
    protected boolean binary;
    protected boolean observer;

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

    public abstract GameVersion getVersion();

    public abstract List<T> getAllTags();

    public boolean isBinary() {
        return binary;
    }

    public boolean hasOnePlayerTag() {
        return getTag() != null;
    }

    public boolean isObserver() {
        return observer;
    }

    public UUID getCampaignHeuristic() {
        return campaignHeuristic;
    }
}
