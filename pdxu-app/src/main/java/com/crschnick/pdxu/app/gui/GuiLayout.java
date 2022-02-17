package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.savegame.FileImporter;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTabPane;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class GuiLayout {

    private StackPane stack;
    private JFXTabPane tabPane;
    private Pane loadingBg;
    private StackPane fileDropOverlay;

    public void setup() {
        tabPane = new JFXTabPane();

        JFXSpinner loading = new JFXSpinner();
        loadingBg = new StackPane(loading);
        loadingBg.getStyleClass().add(GuiStyle.CLASS_LOADING);
        loadingBg.setVisible(false);
        TaskExecutor.getInstance().busyProperty().addListener((c, o, n) -> {
            setBusy(n);
        });
        loadingBg.setMinWidth(Pane.USE_COMPUTED_SIZE);
        loadingBg.setPrefHeight(Pane.USE_COMPUTED_SIZE);

        fileDropOverlay = new StackPane(new FontIcon());
        fileDropOverlay.setAlignment(Pos.CENTER);
        fileDropOverlay.getStyleClass().add("file-drag");
        fileDropOverlay.setVisible(false);

        stack = new StackPane(new Pane(), tabPane, loadingBg, fileDropOverlay);
        stack.setOpacity(0);
    }

    private void fillLayout() {
        var savegameMgrLayout = new BorderPane();
        var menu = GuiMenuBar.createMenu();
        savegameMgrLayout.setTop(menu);
        savegameMgrLayout.setBottom(GuiStatusBar.createStatusBar());
        var pane = GuiSavegameEntryList.createCampaignEntryList();
        savegameMgrLayout.setCenter(pane);
        savegameMgrLayout.setLeft(GuiSavegameCollectionList.createCampaignList());
        var savegameMgrTab = new Tab(null, savegameMgrLayout);

        var launcherLayout = new BorderPane();
        savegameMgrLayout.setTop(GuiMenuBar.createMenu());
        savegameMgrLayout.setBottom(GuiStatusBar.createStatusBar());
        var launcherTab = new Tab(null, launcherLayout);

        tabPane.getTabs().addAll(savegameMgrTab, launcherTab);
    }

    private void setGameLookAndFeel(Game g) {
        Platform.runLater(() -> {
            if (g != null) {
                var bg = GameGuiFactory.ALL.get(g).background();
                stack.getChildren().set(0, bg);
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
        stack.setOnDragOver(event -> {
            if (event.getGestureSource() == null && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        stack.setOnDragEntered(event -> {
            if (event.getGestureSource() == null && event.getDragboard().hasFiles()) {
                fileDropOverlay.setVisible(true);
            }
        });

        stack.setOnDragExited(event -> {
            fileDropOverlay.setVisible(false);
        });

        stack.setOnDragDropped(event -> {
            // Only accept drops from outside the app window
            if (event.getGestureSource() == null && event.getDragboard().hasFiles()) {
                event.setDropCompleted(true);
                Dragboard db = event.getDragboard();
                FileImporter.onFileDrop(db.getFiles());
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
            tabPane.styleProperty().setValue("-fx-font-size: " + Settings.getInstance().fontSize.getValue() + "pt;");
            // Disable focus on startup
            tabPane.requestFocus();

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
