package com.crschnick.pdxu.app.comp;

import javafx.scene.layout.Region;

public abstract class SimpleComp extends Comp<CompStructure<Region>> {

    @Override
    public final CompStructure<Region> createBase() {
        return new SimpleCompStructure<>(createSimple());
    }

    protected abstract Region createSimple();
}
