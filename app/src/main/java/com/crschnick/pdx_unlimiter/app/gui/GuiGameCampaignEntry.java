package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.game.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.RakalyHelper;
import com.crschnick.pdx_unlimiter.app.util.SkanderbegHelper;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiGameCampaignEntry {


    public static <T, I extends SavegameInfo<T>> Node createCampaignEntryNode(GameCampaignEntry<T, I> e) {
        VBox main = new VBox();
        main.setAlignment(Pos.CENTER);
        main.setFillWidth(true);
        main.getProperties().put("entry", e);
        Label l = new Label(e.getDate().toDisplayString());
        l.getStyleClass().add(CLASS_DATE);

        JFXTextField name = new JFXTextField();
        name.getStyleClass().add(CLASS_TEXT_FIELD);
        name.setAlignment(Pos.CENTER);
        name.textProperty().bindBidirectional(e.nameProperty());

        Button del = new JFXButton();
        del.setGraphic(new FontIcon());
        del.setOnMouseClicked((m) -> {
            if (DialogHelper.showSavegameDeleteDialog()) {
                SavegameManagerState.get().<T,I>current().getSavegameCache().delete(e);
            }
        });
        del.getStyleClass().add("delete-button");
        Tooltip.install(del, new Tooltip("Delete savegame"));


        var tagImage =
                SavegameManagerState.get().<T, I>current().getGuiFactory().createImage(e);
        Pane tagPane = new Pane(tagImage.getValue());
        tagPane.setMaxWidth(80);
        HBox tagBar = new HBox(tagPane, l);
        tagBar.getStyleClass().add(CLASS_TAG_BAR);
        tagImage.addListener((change, o, n) -> {
            Platform.runLater(() -> {
                tagPane.getChildren().set(0, n);
            });
        });

        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);

        Button melt = new JFXButton();
        melt.setGraphic(new FontIcon());
        melt.setOnMouseClicked((m) -> {
            var out = GuiSavegameIO.showMeltDialog();
            out.ifPresent(path -> {
                TaskExecutor.getInstance().submitTask(() -> {
                    try {
                        var data = RakalyHelper.meltSavegame(
                                SavegameManagerState.get().<T, I>current().getSavegameCache().getSavegameFile(e));
                        Files.write(path, data);
                    } catch (IOException ex) {
                        ErrorHandler.handleException(ex);
                    }
                }, true);
            });
        });
        melt.getStyleClass().add(CLASS_MELT);
        Tooltip.install(melt, new Tooltip("Melt savegame (convert to non-ironman)"));
        if (e.getInfo() != null && e.getInfo().isIronman()) {
            buttonBar.getChildren().add(melt);
        } else {
            e.infoProperty().addListener((c,o,n) -> {
                if (n.isIronman()) {
                    Platform.runLater(() -> {
                        buttonBar.getChildren().add(0, melt);
                    });
                }
            });
        }

        if (SavegameCache.EU4.contains(e)) {
            GameCampaignEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (GameCampaignEntry<Eu4Tag, Eu4SavegameInfo>) e;
            Button upload = new JFXButton();
            upload.setGraphic(new FontIcon());
            upload.setOnMouseClicked((m) -> {
                RakalyHelper.uploadSavegame(SavegameCache.EU4, eu4Entry);
            });
            upload.getStyleClass().add(CLASS_UPLOAD);
            Tooltip.install(upload, new Tooltip("Upload to Rakaly.com"));
            buttonBar.getChildren().add(upload);


            Button analyze = new JFXButton();
            analyze.setGraphic(new FontIcon());
            analyze.setOnMouseClicked((m) -> {
                RakalyHelper.analyzeEntry(SavegameCache.EU4, eu4Entry);
            });
            analyze.getStyleClass().add(CLASS_ANALYZE);
            Tooltip.install(analyze, new Tooltip("Analyze with Rakaly.com"));
            buttonBar.getChildren().add(analyze);


            Button uploadSkanderbeg = new JFXButton();
            uploadSkanderbeg.setGraphic(new FontIcon());
            uploadSkanderbeg.setOnMouseClicked((m) -> {
                SkanderbegHelper.uploadSavegame(SavegameCache.EU4, eu4Entry);
            });
            uploadSkanderbeg.getStyleClass().add(CLASS_MAP);
            Tooltip.install(uploadSkanderbeg, new Tooltip("Upload to Skanderbeg.pm"));
            buttonBar.getChildren().add(uploadSkanderbeg);
        }

        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            Button open = new JFXButton();
            open.setGraphic(new FontIcon());
            open.getStyleClass().add("open-button");
            Tooltip.install(open, new Tooltip("Open stored savegame location"));
            buttonBar.getChildren().add(open);
            open.setOnMouseClicked((m) -> {
                SavegameActions.openCampaignEntry(e);
            });
        }


        buttonBar.getChildren().add(del);


        BorderPane layout = new BorderPane();
        layout.setLeft(tagBar);
        layout.setCenter(name);

        buttonBar.getStyleClass().add(CLASS_BUTTON_BAR);
        layout.setRight(buttonBar);

        tagBar.setAlignment(Pos.CENTER);
        layout.getStyleClass().add(CLASS_ENTRY_BAR);
        main.getChildren().add(layout);
        Node content = createSavegameInfoNode(e);

        InvalidationListener lis = (change) -> {
            Platform.runLater(() -> {
                layout.setBackground(
                        SavegameManagerState.get().<T, I>current().getGuiFactory().createEntryInfoBackground(e));
            });
        };
        e.infoProperty().addListener(lis);
        if (e.infoProperty().isNotNull().get()) {
            lis.invalidated(null);
        }

        main.getChildren().add(content);
        main.getStyleClass().add(CLASS_ENTRY);
        main.setOnMouseClicked(event -> {
            if (e.infoProperty().isNotNull().get()) {
                SavegameManagerState.get().selectEntry(e);
            }
        });

        main.setOnDragDetected(me -> {
            Dragboard db = main.startDragAndDrop(TransferMode.COPY);
            var sc = SavegameManagerState.get().<T,I>current().getSavegameCache();
            var out = FileUtils.getTempDirectory().toPath().resolve(sc.getFileName(e));
            try {
                sc.exportSavegame(e, out);
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                me.consume();
                return;
            }

            var cc = new ClipboardContent();
            cc.putFiles(List.of(out.toFile()));
            db.setContent(cc);
            me.consume();
        });
        return main;
    }

    private static <T, I extends SavegameInfo<T>> Node createSavegameInfoNode(GameCampaignEntry<T, I> entry) {
        StackPane stack = new StackPane();
        JFXMasonryPane grid = new JFXMasonryPane();
        grid.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER);
        grid.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);
        grid.setHSpacing(10);
        grid.setVSpacing(10);
        grid.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> 3 * grid.getCellHeight() + 2 * grid.getVSpacing() + grid.getPadding().getBottom() + grid.getPadding().getTop(), grid.paddingProperty()));
        grid.setLimitRow(3);

        JFXSpinner loading = new JFXSpinner();
        loading.getStyleClass().add(CLASS_ENTRY_LOADING);
        stack.getChildren().add(grid);
        if (entry.infoProperty().isNotNull().get()) {
            SavegameManagerState.get().<T, I>current().getGuiFactory().fillNodeContainer(entry, grid);
        } else {
            stack.getChildren().add(loading);
        }

        AtomicBoolean load = new AtomicBoolean(false);
        stack.sceneProperty().addListener((c, o, n) -> {
            if (stack.localToScreen(0, 0) == null) {
                return;
            }

            if (stack.localToScreen(0, 0).getY() < PdxuApp.getApp().getScene().getWindow().getHeight() && !load.get()) {
                load.set(true);
                SavegameManagerState.get().<T, I>current().getSavegameCache().loadEntryAsync(entry);
            }
        });

        entry.infoProperty().addListener((change) -> {
            Platform.runLater(() -> {
                loading.setVisible(false);
                SavegameManagerState.get().<T, I>current().getGuiFactory().fillNodeContainer(entry, grid);
            });
        });

        return stack;
    }
}
