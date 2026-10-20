package com.crschnick.pdxu.app.info.eu4;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.savegame.SavegameContent;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

import java.util.ArrayList;
import java.util.List;

public abstract class Eu4SubjectsComp extends Eu4DiplomacyRowComp {

    protected abstract boolean includes(String subjectType);

    @Override
    protected List<Eu4Tag> getTags(SavegameContent content, SavegameData<?> data) {
        var list = new ArrayList<Eu4Tag>();
        var diplomacy = content.get().getNodeForKeyIfExistent("diplomacy");
        if (diplomacy.isEmpty()) {
            return list;
        }

        var player = data.eu4().getTagName();
        for (Node dependency : diplomacy.get().getNodesForKey("dependency")) {
            var first = dependency.getNodeForKeyIfExistent("first").map(Node::getString);
            var second = dependency.getNodeForKeyIfExistent("second").map(Node::getString);
            var type = dependency.getNodeForKeyIfExistent("subject_type")
                    .map(Node::getString)
                    .orElse("");
            if (first.isPresent() && first.get().equals(player) && second.isPresent() && includes(type)) {
                var tag = Eu4Tag.getTag(data.eu4().getAllTags(), second.get());
                if (!list.contains(tag)) {
                    list.add(tag);
                }
            }
        }
        return list;
    }
}
