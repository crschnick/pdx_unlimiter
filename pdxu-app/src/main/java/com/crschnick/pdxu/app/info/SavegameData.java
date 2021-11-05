package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.info.eu4.Eu4SavegameData;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.GameVersion;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.UUID;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = Eu4SavegameData.class)})
public abstract class SavegameData<T> {

    protected GameDate date;
    protected UUID campaignHeuristic;
    protected List<String> mods;
    protected List<String> dlcs;
    protected boolean ironman;
    protected boolean binary;
    protected boolean observer;

    public SavegameData() {
    }

    public abstract T getTag();

    public abstract GameVersion getVersion();

    public abstract List<T> getAllTags();

    public boolean hasOnePlayerTag() {
        return getTag() != null;
    }

    protected abstract void init(ArrayNode node);

    public Eu4SavegameData eu4() {
        return (Eu4SavegameData) this;
    }

    public GameInstallation installation() {
        return null;
    }

    public boolean isIronman() {
        return ironman;
    }

    public GameDate getDate() {
        return date;
    }

    public UUID getCampaignHeuristic() {
        return campaignHeuristic;
    }

    public List<String> getMods() {
        return mods;
    }

    public List<String> getDlcs() {
        return dlcs;
    }

    public boolean isBinary() {
        return binary;
    }

    public boolean isObserver() {
        return observer;
    }
}
