package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("vic3")
@Getter
public class Vic3SavegameData extends SavegameData<Vic3Tag> {

    private String campaignName;
    private GameVersion version;
    private Vic3Tag tag;
    private List<Vic3Tag> allTags;
    private CoatOfArms coatOfArms;

    public Vic3SavegameData() {
    }

    public Vic3Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.VIC3.getCampaignIdHeuristic(content);

        var meta = content.get().getNodeForKey("meta_data");
        campaignName = meta.getNodeForKeyIfExistent("name").map(Node::getString).orElse("?");
        ironman = meta.getNodeForKeyIfExistent("ironman").map(Node::getBoolean).orElse(false);
        date = GameDateType.VIC3.fromString(content.get().getNodeForKeys("meta_data", "game_date").getString());

        coatOfArms = meta.getNodeForKeyIfExistent("flag").map(coatOfArms -> CoatOfArms.fromNode(coatOfArms, s -> null)).orElse(CoatOfArms.empty());

        var countryId = content.get().getNodeForKey("previous_played").getNodeArray().getFirst().getNodeForKey("idtype").getValueNode().getString();
        var country = content.get().getNodeForKey("country_manager").getNodeForKey("database").getNodeForKeyIfExistent(countryId).orElse(null);
        tag = country != null ? new Vic3Tag(countryId, country.getNodeForKey("definition").getString(), country.getNodeForKey("government").getString()) : null;
        allTags = tag != null ? List.of(tag) : List.of();
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
            throw new IllegalArgumentException("Could not parse VIC3 version string: " + v);
        }
    }

    public List<Vic3Tag> getAllTags() {
        return allTags;
    }
}
