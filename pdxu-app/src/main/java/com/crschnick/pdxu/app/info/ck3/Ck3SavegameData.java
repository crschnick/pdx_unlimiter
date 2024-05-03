package com.crschnick.pdxu.app.info.ck3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("ck3")
public class Ck3SavegameData extends SavegameData<Ck3Tag> {

    protected Ck3Tag tag;
    protected List<Ck3Tag> allTags;
    private GameVersion version;

    @Override
    public Ck3Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    public List<Ck3Tag> getAllTags() {
        return allTags;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.CK3.getCampaignIdHeuristic(content);

        var meta = content.get().getNodeForKey("meta_data");
        ironman = meta.getNodeForKeyIfExistent("ironman").map(Node::getBoolean).orElse(false);
        date = GameDateType.CK3.fromString(content.get().getNodeForKey("date").getString());

        allTags = Ck3Tag.fromNode(content.get());
        tag = Ck3Tag.getPlayerTag(content.get(), allTags).orElse(null);
        observer = tag == null;

        mods = content.get().getNodeForKey("meta_data").getNodeForKeyIfExistent("mods")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        dlcs = content.get().getNodeForKey("meta_data").getNodeForKeyIfExistent("dlcs")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());

        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
        var v = n.getNodeForKey("meta_data").getNodeForKey("version").getString();
        Matcher m = p.matcher(v);
        if (m.matches()) {
            var fourth = m.group(4) != null ? Integer.parseInt(m.group(4)) : 0;
            version = new GameVersion(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    fourth
            );
        } else {
            throw new IllegalArgumentException("Could not parse CK3 version string: " + v);
        }
    }
}
