package com.crschnick.pdxu.app.core;

import com.crschnick.pdxu.app.util.OsType;

import javafx.application.Preloader;
import javafx.stage.Stage;

import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class AppPreloader extends Preloader {

    @Override
    @SneakyThrows
    public void start(Stage primaryStage) {
        if (OsType.ofLocal() != OsType.LINUX) {
            return;
        }

        // Do it this way to prevent IDE inspections from complaining
        var c = Class.forName(
                ModuleLayer.boot().findModule("javafx.graphics").orElseThrow(), "com.sun.glass.ui.Application");
        var m = c.getDeclaredMethod("setName", String.class);
        // Set X window name properly
        m.invoke(
                c.getMethod("GetApplication").invoke(null), AppNames.ofCurrent().getName());
    }
}
