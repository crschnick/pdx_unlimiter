package com.crschnick.pdxu.app.info;

import com.crschnick.pdxu.app.info.eu4.Eu4SavegameData;
import com.crschnick.pdxu.io.node.ArrayNode;

public abstract class SavegameData {

    private boolean ironman;

    protected abstract boolean determineIronman(ArrayNode node);

    public Eu4SavegameData eu4() {
        return (Eu4SavegameData) this;
    }

    public boolean isIronman() {
        return ironman;
    }
}
