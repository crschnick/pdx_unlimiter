package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.window.AppDialog;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.Region;

public class ModalOverlayStackComp extends SimpleComp {

    private final Comp<?> background;

    public ModalOverlayStackComp(Comp<?> background) {
        this.background = background;
    }

    @Override
    protected Region createSimple() {
        var current = background;
        for (var i = 0; i < 6; i++) {
            current = buildModalOverlay(current, i);
        }
        return current.createRegion();
    }

    private Comp<?> buildModalOverlay(Comp<?> current, int index) {
        ObservableList<ModalOverlay> modalOverlays = AppDialog.getModalOverlays();
        var prop = new SimpleObjectProperty<>(modalOverlays.size() > index ? modalOverlays.get(index) : null);
        modalOverlays.addListener((ListChangeListener<? super ModalOverlay>) c -> {
            prop.set(modalOverlays.size() > index ? modalOverlays.get(index) : null);
        });
        prop.addListener((observable, oldValue, newValue) -> {
            if (newValue == null && oldValue != null) {
                AppDialog.hide(oldValue);
            }
        });
        var comp = new ModalOverlayComp(current, prop);
        comp.styleClass("modal-overlay-stack-element");
        return comp;
    }
}
