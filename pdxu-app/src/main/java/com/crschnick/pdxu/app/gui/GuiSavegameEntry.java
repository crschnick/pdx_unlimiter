package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdxu.app.gui.dialog.GuiSavegameNotes;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.util.integration.ConverterHelper;
import com.crschnick.pdxu.app.util.integration.Eu4SeHelper;
import com.crschnick.pdxu.app.util.integration.RakalyWebHelper;
import com.crschnick.pdxu.app.util.integration.SkanderbegHelper;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.model.ck3.Ck3Tag;
import com.crschnick.pdxu.model.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.model.eu4.Eu4Tag;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public class GuiSavegameEntry {

    private static Image FILE_ICON;

    private static <T, I extends SavegameInfo<T>> Region setupTopBar(SavegameEntry<T, I> e) {
        BorderPane topBar = new BorderPane();
        topBar.getStyleClass().add(CLASS_ENTRY_BAR);
        SavegameContext.withSavegameAsync(e, ctx -> {
            topBar.setBackground(ctx.getGuiFactory().createEntryInfoBackground(ctx.getInfo()));
        });

        var dateString = e.getDate().toDisplayString(LanguageManager.getInstance().getActiveLanguage().getLocale());
        {
            Label l = new Label(dateString);
            l.getStyleClass().add(CLASS_DATE);

            var tagImage = SavegameContext.mapSavegame(e,
                    ctx -> ctx.getGuiFactory().createImage(e));
            Pane tagPane = new Pane(tagImage.getValue());
            HBox tagBar = new HBox(tagPane, l);
            tagBar.getStyleClass().add(CLASS_TAG_BAR);
            tagImage.addListener((change, o, n) -> {
                Platform.runLater(() -> {
                    tagPane.getChildren().set(0, n);
                });
            });
            tagBar.setAlignment(Pos.CENTER);
            topBar.setLeft(tagBar);
        }
        {
            JFXTextField name = new JFXTextField();
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.setAlignment(Pos.CENTER);
            name.setText(e.getName().equals(dateString) ? "" : e.getName());
            name.textProperty().addListener((c, o, n) -> {
                e.nameProperty().set(n);
            });
            topBar.setCenter(name);
        }
        {
            HBox buttonBar = createButtonBar(e);
            topBar.setRight(buttonBar);
        }
        return topBar;
    }

    private static <T, I extends SavegameInfo<T>> void setupDragAndDrop(Region r, SavegameEntry<T, I> e) {
        r.setOnDragDetected(me -> {
            if (FILE_ICON == null) {
                FILE_ICON = new Image(PdxuInstallation.getInstance().getResourceDir().resolve("img")
                        .resolve("file_icon.png").toUri().toString(), 80, 80, true, false);
            }

            var out = SavegameActions.exportToTemp(e, true);
            out.ifPresent(p -> {
                Dragboard db = r.startDragAndDrop(TransferMode.COPY);
                var cc = new ClipboardContent();
                cc.putFiles(List.of(p.toFile()));
                db.setContent(cc);
                db.setDragView(FILE_ICON, 30, 60);
            });
            me.consume();
        });
    }

    public static <T, I extends SavegameInfo<T>> Node createSavegameEntryNode(SavegameEntry<T, I> e) {
        VBox entryNode = new VBox();
        entryNode.setAlignment(Pos.CENTER);
        entryNode.setFillWidth(true);
        entryNode.getStyleClass().add(CLASS_ENTRY);
        entryNode.setOnMouseClicked(event -> SavegameManagerState.<T, I>get().selectEntry(e));

        setupDragAndDrop(entryNode, e);

        var topBar = setupTopBar(e);
        entryNode.getChildren().add(topBar);
        // Important!
        // Set view order to force top bar to be in front of the savegame info node
        // In case the masonry pane overflows, the top bar can not be blocked that way
        topBar.setViewOrder(-1);

        Node content = createSavegameInfoNode(e);
        entryNode.getChildren().add(content);

        return entryNode;
    }

    private static <T, I extends SavegameInfo<T>> HBox createButtonBar(SavegameEntry<T, I> e) {
        HBox staticButtons = new HBox();
        staticButtons.setAlignment(Pos.CENTER);
        staticButtons.getStyleClass().add(CLASS_BUTTON_BAR);
        {
            Button report = new JFXButton();
            report.setGraphic(new FontIcon());
            report.setOnMouseClicked((m) -> {
                ErrorHandler.reportIssue(SavegameContext.getContext(e).getStorage().getSavegameFile(e));
            });
            report.getStyleClass().add("report-button");
            GuiTooltips.install(report, "Report an issue with the savegame");
            staticButtons.getChildren().add(report);
        }
        {
            Button copy = new JFXButton();
            copy.setGraphic(new FontIcon());
            copy.setOnMouseClicked((m) -> {
                SavegameActions.copySavegame(e);
            });
            copy.getStyleClass().add(CLASS_COPY);
            GuiTooltips.install(copy, "Make a copy of the savegame");
            staticButtons.getChildren().add(copy);
        }
        {
            Button notes = new JFXButton();
            notes.setGraphic(new FontIcon());
            notes.setOnMouseClicked((m) -> {
                GuiSavegameNotes.showSavegameNotesDialog(e.getNotes());
            });
            notes.getStyleClass().add("notes-button");
            GuiTooltips.install(notes, "Edit savegame notes");
            staticButtons.getChildren().add(notes);
        }

        {
            Button del = new JFXButton();
            del.setGraphic(new FontIcon());
            del.setOnMouseClicked((m) -> {
                if (GuiDialogHelper.showBlockingAlert(alert -> {
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm deletion");
                    alert.setHeaderText("Do you want to delete the selected savegame?");
                }).map(t -> t.getButtonData().isDefaultButton()).orElse(false)) {
                    SavegameActions.delete(e);
                }
            });
            del.getStyleClass().add(CLASS_DELETE);
            GuiTooltips.install(del, "Delete savegame");
            staticButtons.getChildren().add(del);
        }

        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            Button open = new JFXButton();
            open.setGraphic(new FontIcon());
            open.getStyleClass().add("open-button");
            GuiTooltips.install(open, "Open stored savegame location");
            staticButtons.getChildren().add(open);
            open.setOnMouseClicked((m) -> {
                SavegameActions.openSavegame(e);
            });
        }


        HBox dynamicButtons = new HBox();
        dynamicButtons.setAlignment(Pos.CENTER);
        dynamicButtons.getStyleClass().add(CLASS_BUTTON_BAR);
        {
            Button melt = new JFXButton();
            melt.setGraphic(new FontIcon());
            melt.setOnMouseClicked((m) -> {
                SavegameActions.meltSavegame(e);
            });
            melt.getStyleClass().add(CLASS_MELT);
            GuiTooltips.install(melt, "Melt savegame (Convert to Non-Ironman)");
            SavegameContext.withSavegameAsync(e, ctx -> {
                if (ctx.getInfo().isBinary()) {
                    dynamicButtons.getChildren().add(melt);
                }
            });
        }

        if (SavegameStorage.ALL.get(Game.EU4) != null && SavegameStorage.ALL.get(Game.EU4).contains(e)) {
            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) e;
            Button upload = new JFXButton();
            upload.setGraphic(new FontIcon());
            upload.setOnMouseClicked((m) -> {
                RakalyWebHelper.uploadSavegame(eu4Entry);
            });
            upload.getStyleClass().add(CLASS_ANALYZE);
            GuiTooltips.install(upload, "Upload and analyze with Rakaly.com");
            dynamicButtons.getChildren().add(upload);


            Button uploadSkanderbeg = new JFXButton();
            uploadSkanderbeg.setGraphic(new FontIcon());
            uploadSkanderbeg.setOnMouseClicked((m) -> {
                SkanderbegHelper.uploadSavegame(eu4Entry);
            });
            uploadSkanderbeg.getStyleClass().add(CLASS_MAP);
            GuiTooltips.install(uploadSkanderbeg, "Upload to Skanderbeg.pm");
            dynamicButtons.getChildren().add(uploadSkanderbeg);
        }

        SavegameContext.withSavegameAsync(e, ctx -> {
            if (Eu4SeHelper.shouldShowButton(e, ctx.getInfo())) {
                Button eu4Se = new JFXButton();
                eu4Se.setGraphic(new FontIcon());
                eu4Se.setOnMouseClicked((m) -> {
                    Eu4SeHelper.open(e);
                });
                eu4Se.getStyleClass().add("eu4se-button");
                GuiTooltips.install(eu4Se, "Open with Eu4SaveEditor");
                dynamicButtons.getChildren().add(0, eu4Se);
            }
        });

        if (SavegameStorage.ALL.get(Game.CK3).contains(e) && Game.EU4.isEnabled()) {
            SavegameEntry<Ck3Tag, Ck3SavegameInfo> ck3Entry = (SavegameEntry<Ck3Tag, Ck3SavegameInfo>) e;
            Button convert = new JFXButton();
            convert.setGraphic(new FontIcon());
            convert.setOnMouseClicked((m) -> {
                ConverterHelper.convertCk3ToEu4(ck3Entry);
            });
            convert.getStyleClass().add(CLASS_CONVERT);
            GuiTooltips.install(convert, "Convert to EU4 savegame");
            dynamicButtons.getChildren().add(convert);
        }

        {
            Button edit = new JFXButton();
            edit.setGraphic(new FontIcon());
            edit.setOnMouseClicked((m) -> {
                SavegameActions.editSavegame(e);
            });
            edit.getStyleClass().add(CLASS_EDIT);
            GuiTooltips.install(edit, "Edit savegame");
            SavegameContext.withSavegameAsync(e, ctx -> {
                if (!ctx.getInfo().isBinary()) {
                    dynamicButtons.getChildren().add(0, edit);
                }
            });
        }


        HBox buttonBar = new HBox(dynamicButtons, staticButtons);
        buttonBar.setSpacing(40);
        buttonBar.setAlignment(Pos.CENTER);
        return buttonBar;
    }

    private static JFXMasonryPane createEmptyContainer() {
        JFXMasonryPane container = new JFXMasonryPane();
        container.getStyleClass().add(CLASS_CAMPAIGN_ENTRY_NODE_CONTAINER);
        container.setLayoutMode(JFXMasonryPane.LayoutMode.MASONRY);
        container.setHSpacing(10);
        container.setVSpacing(10);
        container.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> 3 * container.getCellHeight() +
                        2 * container.getVSpacing() +
                        container.getPadding().getBottom() +
                        container.getPadding().getTop(), container.paddingProperty()));
        container.setLimitRow(3);
        return container;
    }

    private static <T, I extends SavegameInfo<T>> Node createSavegameInfoNode(SavegameEntry<T, I> entry) {
        StackPane stack = new StackPane();

        JFXSpinner loading = new JFXSpinner();
        loading.getStyleClass().add(CLASS_ENTRY_LOADING);
        loading.setVisible(false);

        stack.getChildren().add(loading);
        StackPane.setAlignment(loading, Pos.CENTER);
        stack.getChildren().add(createEmptyContainer());

        Runnable loadEntry = () -> {
            SavegameContext.withSavegame(entry, ctx -> {
                if (ctx.getInfo() != null) {
                    TaskExecutor.getInstance().submitOrRun(() -> {
                        var container = createEmptyContainer();
                        ctx.getGuiFactory().fillNodeContainer(ctx.getInfo(), container);

                        // Clear caches if necessary after being done with loading
                        CacheManager.getInstance().onEntryLoadFinish();

                        Platform.runLater(() -> {
                            loading.setVisible(false);
                            stack.getChildren().set(1, container);
                            container.layout();
                        });
                    });
                }
            });
        };
        loadEntry.run();

        stack.sceneProperty().addListener((c, o, n) -> {
            if (n != null) {
                SavegameManagerState.<T, I>get().loadEntryAsync(entry);
            }
        });

        entry.stateProperty().addListener((c, o, n) -> {
            boolean showLoad = n == SavegameEntry.State.LOADING;
            if (showLoad) {
                Platform.runLater(() -> {
                    loading.setVisible(true);
                });
            }

            boolean failed = n == SavegameEntry.State.LOAD_FAILED;
            if (failed) {
                Platform.runLater(() -> {
                    loading.setVisible(false);
                });
            }
        });

        entry.infoProperty().addListener((c, o, n) -> {
            if (n != null) {
                loadEntry.run();
            } else {
                Platform.runLater(() -> {
                    stack.getChildren().set(1, createEmptyContainer());
                });
            }
        });

        return stack;
    }
}
