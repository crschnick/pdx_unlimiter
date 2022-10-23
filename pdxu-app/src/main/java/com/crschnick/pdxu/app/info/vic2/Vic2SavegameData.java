package com.crschnick.pdxu.app.info.vic2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SavegameInfoException;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.vic2.Vic2Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("vic2")
public class Vic2SavegameData extends SavegameData<Vic2Tag> {

    private GameVersion version;
    private Vic2Tag tag;
    private List<Vic2Tag> allTags;

    public Vic2SavegameData() {
    }

    public Vic2Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    protected void init(SavegameContent content) throws SavegameInfoException {
        campaignHeuristic = SavegameType.VIC2.getCampaignIdHeuristic(content);

        ironman = false;
        date = GameDateType.VIC2.fromString(content.get().getNodeForKey("date").getString());
        binary = false;

        allTags = new ArrayList<>();
        content.get().forEach((k, v) -> {
            if (!k.toUpperCase().equals(k) || k.length() != 3) {
                return;
            }

            allTags.add(new Vic2Tag(k));
        });
        var playerTag = content.get().getNodeForKey("player").getString();
        tag = allTags.stream().filter(t -> t.getTagId().equals(playerTag))
                .findAny()
                .orElseThrow(() -> new SavegameInfoException("No player tag found"));

        mods = null;
        dlcs = null;

        // Hardcode version
        version = new GameVersion(3, 4, 0, 0);
    }

    public List<Vic2Tag> getAllTags() {
        return allTags;
    }
}
