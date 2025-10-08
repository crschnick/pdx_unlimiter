package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.gui.game.GameImage;
import com.crschnick.pdxu.app.savegame.FileImporter;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.AllArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;

@AllArgsConstructor
public class GuiLayoutComp extends SimpleComp {

    private final SavegameManagerState<?, ?> savegameManagerState;

    private BorderPane createLayout() {
        var layout = new BorderPane();
        layout.setBottom(new GuiStatusBarComp<>(savegameManagerState).createRegion());

        var pane = new GuiSavegameEntryListComp<>(savegameManagerState).createRegion();
        layout.setCenter(pane);

        layout.setLeft(new GuiSavegameCollectionListComp<>(savegameManagerState).hide(savegameManagerState.storageEmptyProperty()).createRegion());
        return layout;
    }

    private void setBusy(Region loadingBg, boolean busy) {
        if (!busy) {
            ThreadHelper.createPlatformThread("loading delay", true, () -> {
                ThreadHelper.sleep(50);
                if (!TaskExecutor.getInstance().isBusy()) {
                    Platform.runLater(() -> loadingBg.setVisible(false));
                }
            }).start();
        } else {
            Platform.runLater(() -> loadingBg.setVisible(true));
        }
    }

    private void setupDragAndDrop(StackPane stack, StackPane fileDropOverlay) {
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

    @Override
    protected Region createSimple() {
        var layout = createLayout();

        JFXSpinner loading = new JFXSpinner();
        var loadingBg = new StackPane(loading);
        loadingBg.getStyleClass().add(GuiStyle.CLASS_LOADING);
        loadingBg.setVisible(false);
        TaskExecutor.getInstance().busyProperty().addListener((c, o, n) -> {
            setBusy(loadingBg, n);
        });
        loadingBg.setMinWidth(Pane.USE_COMPUTED_SIZE);
        loadingBg.setPrefHeight(Pane.USE_COMPUTED_SIZE);

        var fileDropIcon = new FontIcon();
        var fileDropOverlay = new StackPane(fileDropIcon);
        fileDropOverlay.setAlignment(Pos.CENTER);
        fileDropOverlay.getStyleClass().add("file-drag");
        fileDropOverlay.setVisible(false);

        var stack = new StackPane(new Pane(), layout, loadingBg, fileDropOverlay);
        setupDragAndDrop(stack, fileDropOverlay);

        stack.sceneProperty().subscribe(scene -> {
            Platform.runLater(() -> {
                if (scene != null) {
                    GameImage.loadGameImages(savegameManagerState.getGame());
                    var bg = GameGuiFactory.ALL.get(savegameManagerState.getGame()).background();
                    stack.getChildren().set(0, bg);
                } else {
                    stack.getChildren().set(0, new Pane());
                }
            });
        });

        return stack;
    }
}
