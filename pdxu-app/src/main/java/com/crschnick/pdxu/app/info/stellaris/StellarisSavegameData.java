package com.crschnick.pdxu.app.info.stellaris;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.GameNamedVersion;
import com.crschnick.pdxu.model.stellaris.StellarisTag;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonTypeName("stellaris")
public class StellarisSavegameData extends SavegameData<StellarisTag> {

    protected StellarisTag tag;
    protected List<StellarisTag> allTags;
    private GameNamedVersion version;



    @Override
    public StellarisTag getTag() {
        return tag;
    }

    @Override
    public GameNamedVersion getVersion() {
        return version;
    }

    @Override
    public List<StellarisTag> getAllTags() {
        return allTags;
    }

    @Override
    protected void init(SavegameContent content) {
        campaignHeuristic = SavegameType.STELLARIS.getCampaignIdHeuristic(content);

        ironman = NodePointer.builder().name("galaxy").name("ironman").build().getIfPresent(content.get())
                .map(Node::getBoolean).orElse(false);
        date = GameDateType.STELLARIS.fromString(content.get().getNodeForKey("date").getString());

        allTags = new ArrayList<>();
        content.get().getNodeForKey("country").forEach((k, v) -> {
            // Invalid country node
            if (v.isValue()) {
                return;
            }

            allTags.add(StellarisTag.fromNode(Long.parseLong(k), v));
        });
        tag = allTags.getFirst();
        tag.setName(content.get().getNodeForKey("name").getString());
        observer = false;

        mods = null;
        dlcs = content.get().getNodeForKeyIfExistent("required_dlcs").map(Node::getNodeArray).orElse(List.of())
                .stream().map(Node::getString)
                .collect(Collectors.toList());

        initVersion(content.get());
    }

    private void initVersion(Node n) {
        Pattern p = Pattern.compile("((?:\\w|\\s)+?)\\s*v?(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
        var vs = n.getNodesForKey("version").getFirst().getString();
        Matcher m = p.matcher(vs);
        if (m.find()) {
           version = new GameNamedVersion(
                    Integer.parseInt(m.group(2)),
                    Integer.parseInt(m.group(3)),
                    m.groupCount() == 5 ? Integer.parseInt(m.group(4)) : 0, 0, m.group(1));
        } else {
            throw new IllegalArgumentException("Invalid Stellaris version: " + vs);
        }
    }
}
