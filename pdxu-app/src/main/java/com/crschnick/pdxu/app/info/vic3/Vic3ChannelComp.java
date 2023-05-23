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

        var base = channel
                .name("channels")
                .name("0")
                .build();
        var index = NodePointer.fromBase(base).name("index").build().getIfPresent(content.get()).orElse(null);
        if (index == null) {
            return;
        }


        value = NodePointer.fromBase(base)
                .name("values")
                .index(index.getInteger() - 1)
                .build()
                .getIfPresent(content.get())
                .map(Node::getDouble)
                .orElse(0.0);
    }
}
