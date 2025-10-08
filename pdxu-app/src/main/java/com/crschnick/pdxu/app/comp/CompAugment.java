package com.crschnick.pdxu.app.comp;

import javafx.scene.Node;
import javafx.scene.layout.Region;

public interface CompAugment<S extends CompStructure<?>> {

    @SuppressWarnings("unchecked")
    default void augment(Node r) {
        augment((S) new SimpleCompStructure<>((Region) r));
    }

    void augment(S struc);
}
