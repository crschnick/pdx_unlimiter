package com.crschnick.pdx_unlimiter.app.editor;

import com.jfoenix.controls.JFXToggleButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class GuiEditor {

    public static Region create() {
        BorderPane layout = new BorderPane();
        layout.setBottom(createFilter());
        return layout;
    }

    private static Region createFilter() {
        HBox box = new HBox();

        ToggleButton keyScope = new JFXToggleButton();
        ToggleButton valueScope = new JFXToggleButton();
        box.getChildren().addAll(keyScope, valueScope);
        return box;
    }
}
