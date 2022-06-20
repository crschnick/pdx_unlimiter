package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.io.node.ArrayNode;
import javafx.scene.layout.Region;

import java.util.List;

public abstract class SavegameInfoMultiComp {

    protected abstract List<? extends SavegameInfoComp> generate(ArrayNode node, SavegameData<?> data);

    public abstract List<? extends Region> create(SavegameData<?> data);
}
