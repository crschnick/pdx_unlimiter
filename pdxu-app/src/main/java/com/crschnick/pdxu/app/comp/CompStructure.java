package com.crschnick.pdxu.app.comp;

import javafx.scene.layout.Region;

public interface CompStructure<R extends Region> {
    R get();
}
