package com.crschnick.pdxu.app.info.vic3;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.SimpleInfoComp;
import com.crschnick.pdxu.io.node.Node;
import com.crschnick.pdxu.io.node.NodePointer;
import com.crschnick.pdxu.io.savegame.SavegameContent;

import java.util.List;

public abstract class Vic3ChannelComp extends SimpleInfoComp {

    protected double value;

    protected abstract List<String> getNames();

    @Override
    protected void init(SavegameContent content, SavegameData<?> data) {
        var channel = NodePointer.builder()
                .name("country_manager")
                .name("database")
                .name(data.vic3().getTag().getId());

        for (String s : getNames()) {
            channel.name(s);
        }

        var base = channel.name("channels").name("0").build();
        var index = NodePointer.fromBase(base)
                .name("index")
                .build()
                .getIfPresent(content.get())
                .orElse(null);
        if (index == null) {
            return;
        }

        var values = NodePointer.fromBase(base)
                .name("values")
                .build()
                .getIfPresent(content.get())
                .filter(Node::isArray)
                .map(Node::getNodeArray)
                .orElse(List.of());
        if (values.isEmpty() || index.getInteger() <= 0) {
            return;
        }

        value = values.get((index.getInteger() - 1) % values.size()).getDouble();
    }
}
