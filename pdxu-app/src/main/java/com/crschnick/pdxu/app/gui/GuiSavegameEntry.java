package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdxu.app.gui.dialog.GuiSavegameNotes;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.LanguageManager;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.*;
import com.crschnick.pdxu.app.util.SupportedOs;
import com.crschnick.pdxu.app.util.integration.ConverterSupport;
import com.crschnick.pdxu.app.util.integration.Eu4SeHelper;
import com.crschnick.pdxu.app.util.integration.RakalyHelper;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
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
        SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
            Platform.runLater(() -> {
                topBar.setBackground(ctx.getGuiFactory().createEntryInfoBackground(ctx.getInfo()));
            });
        });

        var dateString = e.getDate().toDisplayString(LanguageManager.getInstance().getActiveLanguage().getLocale());
        {
            Label l = new Label(dateString);
            l.getStyleClass().add(CLASS_DATE);

            var tagImage = SavegameContext.mapSavegame(e,
                    ctx -> ctx.getGuiFactory().createImage(e));
            HBox tagBar = new HBox(tagImage.getValue(), l);
            tagImage.addListener((c,o,n) -> {
                Platform.runLater(() -> {
                    tagBar.getChildren().set(0, n);
                });
            });
            tagBar.getStyleClass().add(CLASS_TAG_BAR);
            tagBar.setAlignment(Pos.CENTER);
            tagBar.setFillHeight(true);
            topBar.setLeft(tagBar);
        }
        {
            JFXTextField name = new JFXTextField();
            name.setMinWidth(50);
            name.setPrefWidth(50);
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.setAlignment(Pos.CENTER);
            name.setText(e.getName().equals(dateString) ? "" : e.getName());
            name.setAccessibleText("Custom savegame name");
            topBar.setCenter(name);

            var nameChange = new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    e.nameProperty().set(newValue);
                }
            };
            name.textProperty().addListener(nameChange);
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
        entryNode.accessibleTextProperty().bind(e.nameProperty());

        setupDragAndDrop(entryNode, e);

        var topBar = setupTopBar(e);
        entryNode.getChildren().add(topBar);
        // Important!
        // Set view order to force top bar to be in front of the savegame info node
        // In case the masonry pane overflows, the top bar can not be blocked that way
        topBar.setViewOrder(-1);

        Node content = createSavegameInfoNode(e);
        entryNode.getChildren().add(content);

        // For debugging memory leaks
//        for (int i = 0; i < 1000; i++) {
//            var c = createSavegameInfoNode(e);
//        }

        content.setCursor(Cursor.HAND);
        return entryNode;
    }

    @SuppressWarnings("unchecked")
    private static <T, I extends SavegameInfo<T>> HBox createButtonBar(SavegameEntry<T, I> e) {
        HBox staticButtons = new HBox();
        staticButtons.setAlignment(Pos.CENTER);
        staticButtons.getStyleClass().add(CLASS_BUTTON_BAR);
        {
            Button export = new JFXButton(null, new FontIcon());
            export.setGraphic(new FontIcon());
            export.setOnMouseClicked((m) -> {
                SavegameActions.exportSavegame(e);
                SavegameManagerState.<T, I>get().selectEntry(null);
            });
            export.getStyleClass().add(CLASS_EXPORT);
            export.setAccessibleText("Export");
            GuiTooltips.install(export, PdxuI18n.get("EXPORT_SAVEGAME", SavegameContext.getContext(e).getGame().getTranslatedFullName()));
            staticButtons.getChildren().add(export);
        }
        {
            Button report = new JFXButton(null, new FontIcon());
            report.setGraphic(new FontIcon());
            report.setOnMouseClicked((m) -> {
                ErrorHandler.reportIssue(SavegameContext.getContext(e).getStorage().getSavegameFile(e));
            });
            report.getStyleClass().add("report-button");
            report.setAccessibleText("Report");
            GuiTooltips.install(report, PdxuI18n.get("REPORT_SAVEGAME_ISSUE"));
            staticButtons.getChildren().add(report);
        }
        {
            Button copy = new JFXButton(null, new FontIcon());
            copy.setGraphic(new FontIcon());
            copy.setOnMouseClicked((m) -> {
                SavegameActions.copySavegame(e);
            });
            copy.getStyleClass().add(CLASS_COPY);
            copy.setAccessibleText("Copy");
            GuiTooltips.install(copy, PdxuI18n.get("COPY_SAVEGAME"));
            staticButtons.getChildren().add(copy);
        }
        {
            Button notes = new JFXButton(null, new FontIcon());
            notes.setGraphic(new FontIcon());
            notes.setOnMouseClicked((m) -> {
                GuiSavegameNotes.showSavegameNotesDialog(e.getNotes());
            });
            notes.getStyleClass().add("notes-button");
            notes.setAccessibleText("Notes");
            GuiTooltips.install(notes, PdxuI18n.get("EDIT_SAVEGAME_NOTES"));
            staticButtons.getChildren().add(notes);
        }

        {
            Button del = new JFXButton(null, new FontIcon());
            del.setGraphic(new FontIcon());
            del.setOnMouseClicked((m) -> {
                if (GuiDialogHelper.showBlockingAlert(alert -> {
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(PdxuI18n.get("DELETE_SAVEGAME_TITLE"));
                    alert.setHeaderText(PdxuI18n.get("DELETE_SAVEGAME_QUESTION"));
                }).map(t -> t.getButtonData().isDefaultButton()).orElse(false)) {
                    SavegameActions.delete(e);
                }
            });
            del.setAccessibleText("Delete");
            del.getStyleClass().add(CLASS_DELETE);
            GuiTooltips.install(del, PdxuI18n.get("DELETE_SAVEGAME"));
            staticButtons.getChildren().add(del);
        }

        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            Button open = new JFXButton(null, new FontIcon());
            open.setGraphic(new FontIcon());
            open.getStyleClass().add("open-button");
            GuiTooltips.install(open, PdxuI18n.get("OPEN_SAVEGAME"));
            staticButtons.getChildren().add(open);
            open.setOnMouseClicked((m) -> {
                SavegameActions.openSavegame(e);
            });
        }


        HBox dynamicButtons = new HBox();
        dynamicButtons.setAlignment(Pos.CENTER);
        dynamicButtons.getStyleClass().add(CLASS_BUTTON_BAR);


        {
            Button melt = new JFXButton(null, new FontIcon());
            melt.setGraphic(new FontIcon());
            melt.setOnMouseClicked((m) -> {
                SavegameActions.meltSavegame(e);
            });
            melt.getStyleClass().add(CLASS_MELT);
            melt.setAccessibleText("Melt");
            GuiTooltips.install(melt, PdxuI18n.get("MELT_SAVEGAME"));
            SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
                if (ctx.getInfo().getData().isBinary() && RakalyHelper.shouldShowButton(ctx)) {
                    Platform.runLater(() -> {
                        dynamicButtons.getChildren().add(melt);
                    });
                }
            });
        }

        SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
            if (Eu4SeHelper.shouldShowButton(e, ctx.getInfo())) {
                Platform.runLater(() -> {
                    Button eu4Se = new JFXButton(null, new FontIcon());
                    eu4Se.setGraphic(new FontIcon());
                    eu4Se.setOnMouseClicked((m) -> {
                        Eu4SeHelper.open(e);
                    });
                    eu4Se.getStyleClass().add("eu4se-button");
                    eu4Se.setAccessibleText("EU4 Save Editor");
                    GuiTooltips.install(eu4Se, PdxuI18n.get("EDIT_EU4SAVEEDITOR"));
                    dynamicButtons.getChildren().add(0, eu4Se);
                });
            }
        });

        SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
            if (!SavegameBranches.supportsBranching(e)) {
                return;
            }

            Platform.runLater(() -> {
                Button branch = new JFXButton(null, new FontIcon());
                branch.setGraphic(new FontIcon());
                branch.setOnMouseClicked((m) -> {
                    SavegameActions.branch(e);
                });
                branch.getStyleClass().add("branch-button");
                branch.setAccessibleText("Branch");
                GuiTooltips.install(branch, PdxuI18n.get("BRANCH_SAVEGAME"));
                dynamicButtons.getChildren().add(0, branch);
            });
        });

        if (SupportedOs.get().equals(SupportedOs.WINDOWS)) {
            SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
                ConverterSupport.ALL.forEach(converterSupport -> {
                    if (converterSupport.getFromGame().equals(ctx.getGame()) && GameInstallation.ALL.get(converterSupport.getToGame()) != null) {
                        Platform.runLater(() -> {
                            Button convert = new JFXButton(null, new FontIcon());
                            convert.setGraphic(new FontIcon());
                            convert.setOnMouseClicked((m) -> {
                                converterSupport.convert(e);
                            });
                            convert.getStyleClass().add(CLASS_CONVERT);
                            convert.setAccessibleText("Convert to " + converterSupport.getToName() + " savegame");
                            GuiTooltips.install(convert, PdxuI18n.get("CONVERT_TO_" + converterSupport.getToName()));
                            dynamicButtons.getChildren().add(convert);
                        });
                    }
                });
            });
        }

        Button edit = new JFXButton(null, new FontIcon());
        edit.setOnMouseClicked((m) -> {
            SavegameActions.editSavegame(e);
        });
        edit.setAccessibleText("Edit");
        GuiTooltips.install(edit, PdxuI18n.get("EDIT_SAVEGAME"));
        ChangeListener<SavegameEntry.State> stateChange = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends SavegameEntry.State> observable, SavegameEntry.State oldValue, SavegameEntry.State n) {
                boolean add = false;
                if (n.equals(SavegameEntry.State.LOADED)) {
                    boolean binary = e.getInfo().getData().isBinary();
                    Platform.runLater(() -> {
                        edit.setGraphic(new FontIcon(binary ? "mdi-pencil-lock" : "mdi-pencil"));
                    });
                    add = true;
                }
                if (n.equals(SavegameEntry.State.LOAD_FAILED)) {
                    Platform.runLater(() -> {
                        edit.setGraphic(new FontIcon("mdi-pencil"));
                    });
                    add = true;
                }
                if (add) {
                    Platform.runLater(() -> {
                        dynamicButtons.getChildren().add(0, edit);
                    });
                } else {
                    Platform.runLater(() -> {
                        dynamicButtons.getChildren().remove(edit);
                    });
                }

                if (n.equals(SavegameEntry.State.INACTIVE)) {
                    e.stateProperty().removeListener(this);
                }
            }
        };
        stateChange.changed(null, null, e.getState());
        e.stateProperty().addListener(stateChange);

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

        Runnable createEntryContainer = () -> {
            SavegameContext.withSavegameContext(entry, ctx -> {
                if (ctx.getInfo() != null) {
                    TaskExecutor.getInstance().submitOrRun(() -> {
                        var container = ctx.getInfo().createContainer();
                        //ctx.getGuiFactory().fillNodeContainer(ctx.getInfo(), container);

                        Platform.runLater(() -> {
                            loading.setVisible(false);
                            stack.getChildren().set(1, container);
                            container.layout();
                        });
                    });
                }
            });
        };
        createEntryContainer.run();

        entry.stateProperty().addListener(new ChangeListener<SavegameEntry.State>() {
            @Override
            public void changed(ObservableValue<? extends SavegameEntry.State> observable, SavegameEntry.State oldValue, SavegameEntry.State n) {
                boolean showLoad = n == SavegameEntry.State.LOADING;
                if (showLoad) {
                    Platform.runLater(() -> {
                        loading.setVisible(true);
                    });
                }

                boolean loaded = n == SavegameEntry.State.LOADED;
                if (loaded) {
                    createEntryContainer.run();
                }

                boolean failed = n == SavegameEntry.State.LOAD_FAILED;
                if (failed) {
                    Platform.runLater(() -> {
                        loading.setVisible(false);
                        stack.getChildren().set(1, createEmptyContainer());
                    });
                }

                if (n == SavegameEntry.State.INACTIVE) {
                    entry.stateProperty().removeListener(this);
                }
            }
        });

        stack.sceneProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene n) {
                if (n != null) {
                    SavegameManagerState.<T, I>get().loadEntryAsync(entry);
                    stack.sceneProperty().removeListener(this);
                }
            }
        });

        return stack;
    }
}
