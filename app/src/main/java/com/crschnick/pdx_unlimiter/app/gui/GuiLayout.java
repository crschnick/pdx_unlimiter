package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.Settings;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

public class GuiLayout {

    private static BorderPane layout;

    public static StackPane createLayout() {
        layout = new BorderPane();
        var menu = GuiMenuBar.createMenu();
        layout.setTop(menu);
        layout.setBottom(GuiStatusBar.createStatusBar());

        Pane pane = new Pane(new Label());
        layout.setCenter(pane);
        GuiSavegameEntryList.createCampaignEntryList(pane);
        layout.setCenter(pane);

        layout.setLeft(GuiSavegameCollectionList.createCampaignList());
        BorderPane.setAlignment(pane, Pos.CENTER);

        layout.setOnDragOver(event -> {
            if (event.getGestureSource() == null
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        layout.setOnDragDropped(event -> {
            // Only accept drops from outside the app window
            if (event.getGestureSource() == null && event.getDragboard().hasFiles()) {
                event.setDropCompleted(true);
                Dragboard db = event.getDragboard();
                var importTargets = db.getFiles().stream()
                        .map(File::toString)
                        .map(FileImportTarget::createTargets)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                FileImporter.importTargets(importTargets);
            }
            event.consume();
        });

        JFXSpinner loading = new JFXSpinner();
        Pane loadingBg = new StackPane(loading);
        loadingBg.getStyleClass().add(GuiStyle.CLASS_LOADING);
        loadingBg.setVisible(false);
        TaskExecutor.getInstance().busyProperty().addListener((c, o, n) -> {
            if (!n) {
                ThreadHelper.create("loading delay", true, () -> {
                    ThreadHelper.sleep(50);
                    if (!TaskExecutor.getInstance().isBusy()) {
                        Platform.runLater(() -> loadingBg.setVisible(false));
                    }
                }).start();
            } else {
                Platform.runLater(() -> loadingBg.setVisible(true));
            }
        });
        loadingBg.setMinWidth(Pane.USE_COMPUTED_SIZE);
        loadingBg.setPrefHeight(Pane.USE_COMPUTED_SIZE);

        StackPane stack = new StackPane(new Pane(), layout, loadingBg);

        SavegameManagerState.get().currentGameProperty().addListener((c, o, n) -> Platform.runLater(() -> {
            if (n != null) {
                stack.getChildren().set(0, n.getGuiFactory().background());
                try {
                    menu.setOpacity(0.95);
                    stack.styleProperty().set("-fx-font-family: " + n.getGuiFactory().font().getName() + ";");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));

        return stack;
    }

    public static void init() {
        Platform.runLater(() -> {
            layout.styleProperty().setValue("-fx-font-size: " + Settings.getInstance().getFontSize() + "pt;");

            // Disable focus on startup
            layout.requestFocus();
        });
    }
}
