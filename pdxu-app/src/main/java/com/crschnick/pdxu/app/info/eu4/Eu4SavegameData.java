package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonTypeName("eu4")
public class Eu4SavegameData extends SavegameData<Eu4Tag> {

    private GameNamedVersion version;
    private Eu4Tag tag;
    private List<Eu4Tag> allTags;

    public Eu4SavegameData() {
    }

    public Eu4Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
    }

    @Override
    protected void init(ArrayNode node) {
        allTags = new ArrayList<>();
        node.getNodeForKey("countries").forEach((k, v) -> {
            allTags.add(Eu4Tag.fromNode(k, v));
        });

        String player = node.getNodeForKey("player").getString();
        tag = Eu4Tag.getTag(allTags, player);


        date = GameDateType.EU4.fromString(node.getNodeForKey("date").getString());


        Node ver = node.getNodeForKey("savegame_version");
        version = new GameNamedVersion(
                ver.getNodeForKey("first").getInteger(),
                ver.getNodeForKey("second").getInteger(),
                ver.getNodeForKey("third").getInteger(),
                ver.getNodeForKey("forth").getInteger(),
                ver.getNodeForKey("name").getString());


        queryMods(node);
        dlcs = node.getNodeForKeyIfExistent("dlc_enabled").map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());


        campaignHeuristic = UUID.nameUUIDFromBytes(node.getNodeForKey("countries")
                .getNodeForKey("REB").getNodeForKey("decision_seed").getString().getBytes());
    }

    private void queryMods(Node n) {
        // Mod data has changed in 1.31
        if (version.compareTo(new GameVersion(1, 31, 0, 0)) >= 0) {
            var list = new ArrayList<String>();
            n.getNodeForKeyIfExistent("mods_enabled_names").ifPresent(me -> me.forEach((k, v) -> {
                list.add(v.getNodeForKey("filename").getString());
            }, true));
            mods = list;
        } else {
            mods = n.getNodeForKeyIfExistent("mod_enabled").map(Node::getNodeArray).orElse(List.of())
                    .stream().map(Node::getString)
                    .collect(Collectors.toList());
        }
    }

    public List<Eu4Tag> getAllTags() {
        return allTags;
    }
}
