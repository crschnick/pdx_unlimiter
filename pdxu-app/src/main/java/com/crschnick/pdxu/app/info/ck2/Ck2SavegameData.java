package com.crschnick.pdxu.app.info.ck2;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.ck2.Ck2Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonTypeName("ck2")
public class Ck2SavegameData extends SavegameData<Ck2Tag> {

    protected Ck2Tag tag;
    protected List<Ck2Tag> allTags;
    @Getter
    protected String ironmanSaveName;
    private GameVersion version;

    @Override
    public Ck2Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    public List<Ck2Tag> getAllTags() {
        return allTags;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.CK2.getCampaignIdHeuristic(content);

        ironman = content.get().hasKey("ironman");
        date = GameDateType.CK3.fromString(content.get().getNodeForKey("date").getString());
        binary = false;

        tag = new Ck2Tag(content.get().getNodeForKey("player_realm").getString(), content.get().getNodeForKey("player_name").getString());
        allTags = List.of(tag);
        ironmanSaveName = content.get().getNodeForKeyIfExistent("ironman").map(Node::getString).orElse(null);

        mods = null;
        dlcs = null;

        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
        var v = n.getNodesForKey("version").getFirst().getString();
        Matcher m = p.matcher(v);
        if (m.matches()) {
            version = new GameVersion(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4)));
        } else {
            throw new IllegalArgumentException("Could not parse CK3 version string: " + v);
        }
    }
}
