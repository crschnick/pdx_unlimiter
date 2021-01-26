package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.util.ConverterHelper;
import com.crschnick.pdx_unlimiter.app.util.RakalyWebHelper;
import com.crschnick.pdx_unlimiter.app.util.SkanderbegHelper;
import com.crschnick.pdx_unlimiter.core.data.Ck3Tag;
import com.crschnick.pdx_unlimiter.core.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.core.savegame.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiSavegameEntry {


    public static <T, I extends SavegameInfo<T>> Node createCampaignEntryNode(SavegameEntry<T, I> e) {
        VBox main = new VBox();
        main.setAlignment(Pos.CENTER);
        main.setFillWidth(true);

        Label l = new Label(e.getDate().toDisplayString());
        l.getStyleClass().add(CLASS_DATE);

        JFXTextField name = new JFXTextField();
        name.getStyleClass().add(CLASS_TEXT_FIELD);
        name.setAlignment(Pos.CENTER);
        name.textProperty().bindBidirectional(e.nameProperty());


        var tagImage =
                SavegameManagerState.<T, I>get().current().getGuiFactory().createImage(e);
        Pane tagPane = new Pane(tagImage.getValue());
        tagPane.setMaxWidth(80);
        HBox tagBar = new HBox(tagPane, l);
        tagBar.getStyleClass().add(CLASS_TAG_BAR);
        tagImage.addListener((change, o, n) -> {
            Platform.runLater(() -> {
                tagPane.getChildren().set(0, n);
            });
        });


        BorderPane layout = new BorderPane();
        layout.setLeft(tagBar);
        layout.setCenter(name);


        HBox buttonBar = new HBox();
        buttonBar.setAlignment(Pos.CENTER);
        createButtonBar(e, buttonBar);
        buttonBar.getStyleClass().add(CLASS_BUTTON_BAR);
        layout.setRight(buttonBar);

        tagBar.setAlignment(Pos.CENTER);
        layout.getStyleClass().add(CLASS_ENTRY_BAR);
        main.getChildren().add(layout);
        Node content = createSavegameInfoNode(e);

        ChangeListener<I> lis = (c, o, n) -> {
            Platform.runLater(() -> {
                layout.setBackground(n != null ?
                        SavegameManagerState.<T, I>get().current().getGuiFactory().createEntryInfoBackground(e) : null);
            });
        };
        e.infoProperty().addListener(lis);
        if (e.infoProperty().isNotNull().get()) {
            layout.setBackground(
                    SavegameManagerState.<T, I>get().current().getGuiFactory().createEntryInfoBackground(e));
        }

        main.getChildren().add(content);
        main.getStyleClass().add(CLASS_ENTRY);
        main.setOnMouseClicked(event -> {
            if (e.infoProperty().isNotNull().get()) {
                SavegameManagerState.<T, I>get().selectEntry(e);
            }
        });

        main.setOnDragDetected(me -> {
            Dragboard db = main.startDragAndDrop(TransferMode.COPY);
            var sc = SavegameManagerState.<T, I>get().current().getSavegameCache();
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

    private static <T, I extends SavegameInfo<T>> void createButtonBar(SavegameEntry<T, I> e, HBox buttonBar) {
        {
            Button copy = new JFXButton();
            copy.setGraphic(new FontIcon());
            copy.setOnMouseClicked((m) -> {
                SavegameActions.copySavegame(e);
            });
            copy.getStyleClass().add(CLASS_COPY);
            GuiTooltips.install(copy, "Make a copy of the savegame");
            buttonBar.getChildren().add(copy);
        }

        {
            Button melt = new JFXButton();
            melt.setGraphic(new FontIcon());
            melt.setOnMouseClicked((m) -> {
                SavegameActions.meltSavegame(e);
            });
            melt.getStyleClass().add(CLASS_MELT);
            GuiTooltips.install(melt, "Melt savegame (Convert to Non-Ironman)");
            if (e.getInfo() != null && e.getInfo().isBinary()) {
                buttonBar.getChildren().add(melt);
            } else {
                e.infoProperty().addListener((c, o, n) -> {
                    if (n != null && n.isBinary()) {
                        Platform.runLater(() -> buttonBar.getChildren().add(0, melt));
                    } else {
                        Platform.runLater(() -> buttonBar.getChildren().remove(melt));
                    }
                });
            }
        }

        if (SavegameCache.EU4.contains(e)) {
            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) e;
            Button upload = new JFXButton();
            upload.setGraphic(new FontIcon());
            upload.setOnMouseClicked((m) -> {
                RakalyWebHelper.uploadSavegame(SavegameCache.EU4, eu4Entry);
            });
            upload.getStyleClass().add(CLASS_UPLOAD);
            GuiTooltips.install(upload, "Upload to Rakaly.com");
            buttonBar.getChildren().add(upload);


//            Button analyze = new JFXButton();
//            analyze.setGraphic(new FontIcon());
//            analyze.setOnMouseClicked((m) -> {
//                RakalyHelper.analyzeEntry(SavegameCache.EU4, eu4Entry);
//            });
//            analyze.getStyleClass().add(CLASS_ANALYZE);
//            GuiTooltips.install(analyze, "Analyze with Rakaly.com");
//            buttonBar.getChildren().add(analyze);


            Button uploadSkanderbeg = new JFXButton();
            uploadSkanderbeg.setGraphic(new FontIcon());
            uploadSkanderbeg.setOnMouseClicked((m) -> {
                SkanderbegHelper.uploadSavegame(SavegameCache.EU4, eu4Entry);
            });
            uploadSkanderbeg.getStyleClass().add(CLASS_MAP);
            GuiTooltips.install(uploadSkanderbeg, "Upload to Skanderbeg.pm");
            buttonBar.getChildren().add(uploadSkanderbeg);
        }

        if (SavegameCache.CK3 != null && SavegameCache.CK3.contains(e)) {
            SavegameEntry<Ck3Tag, Ck3SavegameInfo> ck3Entry = (SavegameEntry<Ck3Tag, Ck3SavegameInfo>) e;
            Button convert = new JFXButton();
            convert.setGraphic(new FontIcon());
            convert.setOnMouseClicked((m) -> {
                ConverterHelper.convertCk3ToEu4(ck3Entry);
            });
            convert.getStyleClass().add(CLASS_CONVERT);
            GuiTooltips.install(convert, "Convert to EU4 savegame");
            buttonBar.getChildren().add(convert);
        }

        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            Button open = new JFXButton();
            open.setGraphic(new FontIcon());
            open.getStyleClass().add("open-button");
            GuiTooltips.install(open, "Open stored savegame location");
            buttonBar.getChildren().add(open);
            open.setOnMouseClicked((m) -> {
                SavegameActions.openCampaignEntry(e);
            });
        }

        {
            Button edit = new JFXButton();
            edit.setGraphic(new FontIcon());
            edit.setOnMouseClicked((m) -> {
                SavegameActions.editSavegame(e);
            });
            edit.getStyleClass().add(CLASS_EDIT);
            GuiTooltips.install(edit, "Edit savegame");

            if (e.getInfo() != null && !e.getInfo().isBinary()) {
                buttonBar.getChildren().add(0, edit);
            } else {
                e.infoProperty().addListener((c, o, n) -> {
                    if (n != null && !n.isBinary()) {
                        Platform.runLater(() -> {
                            buttonBar.getChildren().add(0, edit);
                        });
                    } else {
                        Platform.runLater(() -> {
                            buttonBar.getChildren().remove(edit);
                        });
                    }
                });
            }
        }

        {
            Button del = new JFXButton();
            del.setGraphic(new FontIcon());
            del.setOnMouseClicked((m) -> {
                if (DialogHelper.showSavegameDeleteDialog()) {
                    SavegameManagerState.<T, I>get().current().getSavegameCache().deleteAsync(e);
                }
            });
            del.getStyleClass().add(CLASS_DELETE);
            GuiTooltips.install(del, "Delete savegame");
            buttonBar.getChildren().add(del);
        }
    }

    private static <T, I extends SavegameInfo<T>> Node createSavegameInfoNode(SavegameEntry<T, I> entry) {
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
            SavegameManagerState.<T, I>get().current().getGuiFactory().fillNodeContainer(entry.getInfo(), grid);
        } else {
            stack.getChildren().add(loading);
        }

        stack.sceneProperty().addListener((c, o, n) -> {
            if (n != null) {
                SavegameCache.getForSavegame(entry).loadEntryAsync(entry);
            }
        });

        entry.infoProperty().addListener((c, o, n) -> {
            if (n != null) {
                var gf = GameIntegration.getForSavegameCache(SavegameCache.getForSavegame(entry));
                var info = entry.getInfo();
                Platform.runLater(() -> {
                    loading.setVisible(false);
                    gf.getGuiFactory().fillNodeContainer(info, grid);
                });
            } else {
                Platform.runLater(() -> {
                    loading.setVisible(true);
                    grid.getChildren().clear();
                });
            }
        });

        return stack;
    }
}
