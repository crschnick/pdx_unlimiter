package com.crschnick.pdxu.app.comp.base;

import javafx.scene.control.Button;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.function.Consumer;

@Value
public class ModalButton {
    String key;
    Runnable action;
    boolean close;
    boolean defaultButton;

    @NonFinal
    Consumer<Button> augment;

    public ModalButton(String key, Runnable action, boolean close, boolean defaultButton) {
        this.key = key;
        this.action = action;
        this.close = close;
        this.defaultButton = defaultButton;
    }

    public static ModalButton ok(Runnable action) {
        return new ModalButton("ok", action, true, true);
    }

    public static ModalButton ok() {
        return new ModalButton("ok", null, true, true);
    }

    public static ModalButton cancel() {
        return cancel(null);
    }

    public static ModalButton cancel(Runnable action) {
        return new ModalButton("cancel", action, true, false);
    }
}
