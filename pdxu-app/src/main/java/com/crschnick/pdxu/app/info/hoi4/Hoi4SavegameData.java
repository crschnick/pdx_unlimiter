package com.crschnick.pdxu.app.info.hoi4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.hoi4.Hoi4Tag;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("hoi4")
public class Hoi4SavegameData extends SavegameData<Hoi4Tag> {

    protected Hoi4Tag tag;
    protected String cosmeticTag;
    protected List<Hoi4Tag> allTags;
    private GameNamedVersion version;

    @Override
    public Hoi4Tag getTag() {
        return tag;
    }

    public String getCosmeticTag() {
        return cosmeticTag;
    }

    @Override
    public GameNamedVersion getVersion() {
        return version;
    }

    @Override
    public List<Hoi4Tag> getAllTags() {
        return allTags;
    }

    private static List<Hoi4Tag> createAllTags(SavegameContent content) {
        var tags = new ArrayList<Hoi4Tag>();
        content.get().getNodeForKeyIfExistent("countries").ifPresent(countries -> countries.forEach((k, v) -> {
            if (k == null || !v.isArray()) {
                return;
            }

            var ideology = v.getNodeForKeysIfExistent("politics", "ruling_party")
                    .filter(Node::isValue)
                    .map(Node::getString)
                    .orElse(null);
            tags.add(new Hoi4Tag(k, ideology));
        }));
        return tags;
    }

    @Override
    public boolean isPlayableTag(SavegameContent content, Hoi4Tag tag) {
        return content.get()
                        .getNodeForKeysIfExistent("countries", tag.getTag(), "capital")
                        .filter(Node::isValue)
                        .map(Node::getInteger)
                        .orElse(0)
                > 0;
    }

    @Override
    protected void init(SavegameContent content) {
        var player = content.get().getNodeForKey("player").getString();
        // The player country might not have an entry, e.g. after it got annexed
        var tag = content.get().getNodeForKeysIfExistent("countries", player).isPresent()
                ? new Hoi4Tag(player, content.get().getNodeForKey("ideology").getString())
                : null;
        init(content, tag, createAllTags(content));
    }

    @Override
    protected void init(SavegameContent content, Hoi4Tag tag, List<Hoi4Tag> allTags) {
        campaignHeuristic = SavegameType.HOI4.getCampaignIdHeuristic(content);
        this.tag = tag;
        this.allTags = allTags;
        observer = tag == null;
        if (tag != null) {
            var isPlayer = tag.getTag().equals(content.get().getNodeForKey("player").getString());
            cosmeticTag = (isPlayer
                            ? content.get().getNodeForKeyIfExistent("cosmetic_tag")
                            : content.get().getNodeForKeysIfExistent("countries", tag.getTag(), "cosmetic_tag"))
                    .filter(Node::isValue)
                    .map(Node::getString)
                    .filter(s -> !s.isBlank())
                    .orElse(null);
        }
        date = GameDateType.HOI4.fromString(content.get().getNodeForKey("date").getString());
        ironman = content.get().hasKey("ironman");
        mods = content.get().getNodeForKeyIfExistent("mods").map(Node::getNodeArray).orElse(List.of()).stream()
                .map(Node::getString)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        dlcs = null;
        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("([\\w ]+)\\s+v(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d{1,2}))?(?:\\.(\\w+))?\\s+.*");
        var string = n.getNodeForKey("version").getString();
        Matcher m = p.matcher(string);
        if (m.matches()) {
            version = new GameNamedVersion(
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    Integer.parseInt(m.group(4)),
                    m.group(5) != null ? Integer.parseInt(m.group(5)) : 0,
                    m.group(1));
        } else {
            throw new IllegalArgumentException("Could not parse HOI4 version string: " + string);
        }
    }
}
