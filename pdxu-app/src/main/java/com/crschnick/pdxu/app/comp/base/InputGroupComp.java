package com.crschnick.pdxu.app.comp.base;

import atlantafx.base.layout.InputGroup;
import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.CompStructure;
import com.crschnick.pdxu.app.comp.SimpleCompStructure;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import lombok.Setter;

import java.util.List;

public class InputGroupComp extends Comp<CompStructure<HBox>> {

    private final List<Comp<?>> entries;

    @Setter
    private Comp<?> heightReference;

    @Setter
    private boolean mergeComps = true;

    public InputGroupComp(List<Comp<?>> comps) {
        entries = List.copyOf(comps);
    }

    @Override
    public CompStructure<HBox> createBase() {
        HBox b = mergeComps ? new InputGroup() : new HBox();
        b.getStyleClass().add("input-group-comp");
        for (var entry : entries) {
            b.getChildren().add(entry.createRegion());
        }
        b.setAlignment(Pos.CENTER);

        if (heightReference != null && entries.contains(heightReference)) {
            var refIndex = entries.indexOf(heightReference);
            var ref = b.getChildren().get(refIndex);
            if (ref instanceof Region refR) {
                for (int i = 0; i < entries.size(); i++) {
                    if (i == refIndex) {
                        continue;
                    }

                    var entry = b.getChildren().get(i);
                    if (!(entry instanceof Region entryR)) {
                        continue;
                    }

                    entryR.minHeightProperty().bind(refR.heightProperty());
                    entryR.maxHeightProperty().bind(refR.heightProperty());
                    entryR.prefHeightProperty().bind(refR.heightProperty());
                }
            }
        }

        return new SimpleCompStructure<>(b);
    }
}
