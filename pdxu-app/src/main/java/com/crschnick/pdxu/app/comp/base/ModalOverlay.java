package com.crschnick.pdxu.app.comp.base;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.platform.LabelGraphic;

import javafx.beans.value.ObservableValue;

import lombok.*;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.List;

@Value
@With
@Builder(toBuilder = true)
public class ModalOverlay {

    ObservableValue<String> title;
    Comp<?> content;
    LabelGraphic graphic;

    @Singular
    List<Object> buttons;

    @NonFinal
    @Setter
    boolean hasCloseButton;

    @NonFinal
    @Setter
    boolean requireCloseButtonForClose;

    public static ModalOverlay of(Comp<?> content) {
        return of((ObservableValue<String>) null, content, null);
    }

    public static ModalOverlay of(String titleKey, Comp<?> content) {
        return of(titleKey, content, null);
    }

    public static ModalOverlay of(String titleKey, Comp<?> content, LabelGraphic graphic) {
        return of(titleKey != null ? AppI18n.observable(titleKey) : null, content, graphic);
    }

    public static ModalOverlay of(ObservableValue<String> title, Comp<?> content, LabelGraphic graphic) {
        return new ModalOverlay(title, content, graphic, new ArrayList<>(), true, false);
    }

    public ModalOverlay withDefaultButtons(Runnable action) {
        addButton(ModalButton.cancel());
        addButton(ModalButton.ok(action));
        return this;
    }

    public ModalButton addButton(ModalButton button) {
        buttons.add(button);
        return button;
    }

    public void addButtonBarComp(Comp<?> comp) {
        buttons.add(comp);
    }

    public void persist() {
        this.hasCloseButton = false;
        this.requireCloseButtonForClose = true;
    }

    public void show() {
        AppDialog.show(this, false);
    }

    public void hide() {
        AppDialog.hide(this);
    }

    public boolean isShowing() {
        return AppDialog.getModalOverlays().contains(this);
    }

    public void showAndWait() {
        AppDialog.showAndWait(this);
    }

    public void close() {
        AppDialog.closeDialog(this);
    }
}
