package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.gui.game.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.game.GameImage;
import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.savegame.FileImportTarget;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.jfoenix.controls.JFXSpinner;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

public class GuiLayout {

    private StackPane stack;
    private BorderPane layout;
    private Pane loadingBg;

    public void setup() {
        layout = new BorderPane();

        JFXSpinner loading = new JFXSpinner();
        loadingBg = new StackPane(loading);
        loadingBg.getStyleClass().add(PdxuStyle.CLASS_LOADING);
        loadingBg.setVisible(false);
        TaskExecutor.getInstance().busyProperty().addListener((c, o, n) -> {
            setBusy(n);
        });
        loadingBg.setMinWidth(Pane.USE_COMPUTED_SIZE);
        loadingBg.setPrefHeight(Pane.USE_COMPUTED_SIZE);

        stack = new StackPane(new Pane(), layout, loadingBg);
        stack.setOpacity(0);
    }

    private void fillLayout() {
        var menu = GuiMenuBar.createMenu();
        layout.setTop(menu);

        layout.setBottom(GuiStatusBar.createStatusBar());

        var pane = GuiSavegameEntryList.createCampaignEntryList();
        layout.setCenter(pane);

        layout.setLeft(GuiSavegameCollectionList.createCampaignList());
    }

    private void setGameLookAndFeel(Game g) {
        Platform.runLater(() -> {
            if (g != null) {
                var bg = GameGuiFactory.ALL.get(g).background();
                stack.getChildren().set(0, bg);
                try {
                    stack.styleProperty().set("-fx-font-family: " +
                            GameGuiFactory.ALL.get(g).font().getName() + ";");
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                }
            } else {
                stack.getChildren().set(0, new Pane());
            }
        });
    }

    private void setBusy(boolean busy) {
        if (!busy) {
            ThreadHelper.create("loading delay", true, () -> {
                ThreadHelper.sleep(50);
                if (!TaskExecutor.getInstance().isBusy()) {
                    Platform.runLater(() -> loadingBg.setVisible(false));
                }
            }).start();
        } else {
            Platform.runLater(() -> loadingBg.setVisible(true));
        }
    }

    private void setupDragAndDrop() {
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
    }

    public void finishSetup() {
        SavegameManagerState.get().onGameChange(n -> {
            GameImage.loadGameImages(n);
            setGameLookAndFeel(n);
        });

        Platform.runLater(() -> {
            // Set font size
            layout.styleProperty().setValue("-fx-font-size: " + Settings.getInstance().fontSize.getValue() + "pt;");
            // Disable focus on startup
            layout.requestFocus();

            fillLayout();
            setupDragAndDrop();

            FadeTransition ft = new FadeTransition(Duration.millis(1500), stack);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        });
    }

    public Region getContent() {
        return stack;
    }
}
