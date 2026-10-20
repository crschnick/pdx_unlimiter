package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.coa.CoatOfArms;
import com.crschnick.pdxu.model.vic3.Vic3Tag;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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
    private String ironmanName;

    public Vic3SavegameData() {}

    public Optional<Node> getPlayerCountry(SavegameContent content) {
        return content.get()
                .getNodeForKeyIfExistent("country_manager")
                .flatMap(cm -> cm.getNodeForKeyIfExistent("database"))
                .flatMap(db -> db.getNodeForKeyIfExistent(tag.getId()));
    }

    public Vic3Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    private static List<Vic3Tag> createAllTags(SavegameContent content) {
        var tags = new ArrayList<Vic3Tag>();
        content.get().getNodeForKeysIfExistent("country_manager", "database").ifPresent(db -> db.forEach((k, v) -> {
            if (k == null || !v.isArray()) {
                return;
            }

            var definition = v.getNodeForKeyIfExistent("definition").filter(Node::isValue);
            var government = v.getNodeForKeyIfExistent("government").filter(Node::isValue);
            if (definition.isEmpty() || government.isEmpty()) {
                return;
            }

            tags.add(new Vic3Tag(k, definition.get().getString(), government.get().getString()));
        }));
        return tags;
    }

    @Override
    public boolean isPlayableTag(SavegameContent content, Vic3Tag tag) {
        return content.get()
                .getNodeForKeysIfExistent("country_manager", "database", tag.getId())
                .filter(Node::isArray)
                .map(c -> c.hasKey("capital"))
                .orElse(false);
    }

    @Override
    protected void init(SavegameContent content) {
        var countryId = getPlayerId(content);
        var country = countryId != null
                ? content.get().getNodeForKeysIfExistent("country_manager", "database", countryId)
                : Optional.<Node>empty();
        var tag = country.filter(Node::isArray)
                .map(c -> new Vic3Tag(
                        countryId,
                        c.getNodeForKey("definition").getString(),
                        c.getNodeForKey("government").getString()))
                .orElse(null);
        init(content, tag, createAllTags(content));
    }

    private static String getPlayerId(SavegameContent content) {
        var countryIdPointer = NodePointer.builder()
                .name("previous_played")
                .index(0)
                .name("idtype")
                .build();
        var id = countryIdPointer.get(content.get());
        return id != null ? id.getString() : null;
    }

    @Override
    protected void init(SavegameContent content, Vic3Tag tag, List<Vic3Tag> allTags) {
        campaignHeuristic = SavegameType.VIC3.getCampaignIdHeuristic(content);

        var meta = content.get().getNodeForKey("meta_data");
        campaignName = meta.getNodeForKeyIfExistent("name").map(Node::getString).orElse("?");
        ironman = meta.getNodeForKeyIfExistent("ironman").map(Node::getBoolean).orElse(false);
        date = GameDateType.VIC3.fromString(
                content.get().getNodeForKeys("meta_data", "game_date").getString());
        ironmanName = content.get()
                .getNodeForKeysIfExistent("ironman", "save_game")
                .map(Node::getString)
                .filter(s -> !s.isEmpty())
                .orElse(null);

        var isPlayer = tag != null && tag.getId().equals(getPlayerId(content));
        coatOfArms = isPlayer
                ? meta.getNodeForKeyIfExistent("flag")
                        .map(coatOfArms -> CoatOfArms.fromNode(coatOfArms, s -> null))
                        .orElse(CoatOfArms.empty())
                : CoatOfArms.empty();

        this.tag = tag;
        this.allTags = allTags;
        observer = tag == null;

        mods = content
                .get()
                .getNodeForKey("meta_data")
                .getNodeForKeyIfExistent("mods")
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        dlcs = content
                .get()
                .getNodeForKey("meta_data")
                .getNodeForKeyIfExistent("dlcs")
                .map(Node::getNodeArray)
                .orElse(List.of())
                .stream()
                .map(Node::getString)
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
                    Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), fourth);
        } else {
            throw new IllegalArgumentException("Could not parse VIC3 version string: " + v);
        }
    }

    public List<Vic3Tag> getAllTags() {
        return allTags;
    }
}
