package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

import java.util.ArrayList;
import java.util.List;

public class Eu4SavegameData extends SavegameData {

    private final Eu4Tag tag;
    private final List<Eu4Tag> allTags;

    public Eu4SavegameData(ArrayNode node) {
        allTags = new ArrayList<>();
        node.getNodeForKey("countries").forEach((k, v) -> {
            allTags.add(Eu4Tag.fromNode(k, v));
        });

        String player = node.getNodeForKey("player").getString();
        tag = Eu4Tag.getTag(allTags, player);
    }

    public Eu4Tag getTag() {
        return tag;
    }

    public List<Eu4Tag> getAllTags() {
        return allTags;
    }

    @Override
    protected boolean determineIronman(ArrayNode node) {
        return false;
    }
}
