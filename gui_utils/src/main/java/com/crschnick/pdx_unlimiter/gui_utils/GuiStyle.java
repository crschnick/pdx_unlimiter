package com.crschnick.pdx_unlimiter.gui_utils;


import com.crschnick.pdx_unlimiter.sentry_utils.ErrorHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiStyle {

    private final Consumer<Scene> sceneConsumer;
    private final List<String> stylesheets;

    public GuiStyle(Consumer<Scene> sceneConsumer, List<String> stylesheets) {
        this.sceneConsumer = sceneConsumer;
        this.stylesheets = new ArrayList<>(stylesheets);
        this.stylesheets.add(GuiStyle.class.getResource("com.crschnick.pdx_unlimiter.gui_style.empty-alert.css").toString());
    }

    public void applyToAlert(Alert a) {
        a.getDialogPane().getScene().getStylesheets().addAll(stylesheets);
        sceneConsumer.accept(a.getDialogPane().getScene());
    }

    public void applyToScene(Scene scene) {
        scene.getStylesheets().addAll(stylesheets);
        sceneConsumer.accept(scene);
    }

    public static GuiStyle create(Path style, Consumer<Scene> sceneConsumer) {
        if (Files.isDirectory(style)) {
            try {
                var s = Files.list(style)
                        .map(p -> p.toUri().toString())
                        .collect(Collectors.toList());
                return new GuiStyle(sceneConsumer, s);
            } catch (IOException e) {
                ErrorHandler.handleException(e);
                return new GuiStyle(sceneConsumer, List.of());
            }
        } else {
            return new GuiStyle(sceneConsumer, List.of(style.toUri().toString()));
        }
    }
}
