package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.info.ck2.Ck2SavegameData;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameData;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameData;
import com.crschnick.pdxu.app.info.hoi4.Hoi4SavegameData;
import com.crschnick.pdxu.app.info.stellaris.StellarisSavegameData;
import com.crschnick.pdxu.app.info.vic2.Vic2SavegameData;
import com.crschnick.pdxu.app.info.vic3.Vic3SavegameData;
import com.crschnick.pdxu.app.installation.GameFileContext;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.GameDate;
import com.crschnick.pdxu.model.GameVersion;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = Eu4SavegameData.class),
                @JsonSubTypes.Type(value = Ck3SavegameData.class),
                @JsonSubTypes.Type(value = StellarisSavegameData.class),
                @JsonSubTypes.Type(value = Hoi4SavegameData.class),
                @JsonSubTypes.Type(value = Vic2SavegameData.class),
                @JsonSubTypes.Type(value = Vic3SavegameData.class),
                @JsonSubTypes.Type(value = Ck2SavegameData.class)
        }
)
public abstract class SavegameData<T> {

    protected GameDate date;
    protected UUID campaignHeuristic;
    protected LinkedHashSet<String> mods;
    protected List<String> dlcs;
    protected boolean ironman;
    @Setter
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

    protected abstract void init(SavegameContent content) throws Exception;

    public Eu4SavegameData eu4() {
        return (Eu4SavegameData) this;
    }

    public StellarisSavegameData stellaris() {
        return (StellarisSavegameData) this;
    }

    public Ck3SavegameData ck3() {
        return (Ck3SavegameData) this;
    }

    public Ck2SavegameData ck2() {
        return (Ck2SavegameData) this;
    }

    public Vic3SavegameData vic3() {
        return (Vic3SavegameData) this;
    }

    public GameInstallation installation() {
        return GameInstallation.ALL.get(GameFileContext.INFO_MAP.get(getClass()));
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

    public LinkedHashSet<String> getMods() {
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
