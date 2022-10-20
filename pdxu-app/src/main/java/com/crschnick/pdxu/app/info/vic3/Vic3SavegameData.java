package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.vic3.Vic3Tag;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("vic3")
public class Vic3SavegameData extends SavegameData<Vic3Tag> {

    private GameVersion version;
    private Vic3Tag tag;
    private List<Vic3Tag> allTags;

    public Vic3SavegameData() {
    }

    public String getTagName() {
        return null;// tag.getTag();
    }

    public Vic3Tag getTag() {
        return tag;
    }

    @Override
    public GameVersion getVersion() {
        return version;
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
                    fourth);
        } else {
            throw new IllegalArgumentException("Could not parse CK3 version string: "+ v);
        }
    }

    @Override
    protected void init(SavegameContent content) {
//        allTags = new ArrayList<>();
//        node.getNodeForKey("countries").forEach((k, v) -> {
//            allTags.add(Vic3Tag.fromNode(k, v));
//        });
//
//        String player = node.getNodeForKey("player").getString();
//        tag = Eu4Tag.getTag(allTags, player);


        date = GameDateType.VIC3.fromString(content.get().getNodeForKey("date").getString());


        Node ver = content.get().getNodeForKey("savegame_version");
        version = new GameNamedVersion(
                ver.getNodeForKey("first").getInteger(),
                ver.getNodeForKey("second").getInteger(),
                ver.getNodeForKey("third").getInteger(),
                ver.getNodeForKey("forth").getInteger(),
                ver.getNodeForKey("name").getString());


        mods = content.get().getNodeForKey("meta_data").getNodeForKeyIfExistent("mods")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());
        dlcs = content.get().getNodeForKey("meta_data").getNodeForKeyIfExistent("dlcs")
                .map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());


        campaignHeuristic = UUID.nameUUIDFromBytes(content.get().getNodeForKey("countries")
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

    public List<Vic3Tag> getAllTags() {
        return allTags;
    }
}
