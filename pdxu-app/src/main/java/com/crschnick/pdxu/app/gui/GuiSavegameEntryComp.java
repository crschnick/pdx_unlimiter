package com.crschnick.pdxu.app.gui;

import atlantafx.base.controls.Spacer;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppResources;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.TaskExecutor;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.gui.dialog.GuiSavegameNotes;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameBranches;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.util.ConverterSupport;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.RakalyHelper;
import com.jfoenix.controls.JFXMasonryPane;
import com.jfoenix.controls.JFXSpinner;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import lombok.AllArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

@AllArgsConstructor
public class GuiSavegameEntryComp<T, I extends SavegameInfo<T>> extends SimpleComp {

    private static Image FILE_ICON;

    private final SavegameEntry<T, I> e;
    private final SavegameManagerState<T, I> savegameManagerState;

    private Region setupTopBar(SavegameEntry<T, I> e) {
        BorderPane topBar = new BorderPane();
        topBar.getStyleClass().add(CLASS_ENTRY_BAR);
        SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
            Platform.runLater(() -> {
                var bg = ctx.getGuiFactory().createEntryInfoBackground(ctx.getInfo());
                if (bg != null) {
                    topBar.setBackground(bg);
                } else {
                    topBar.getStyleClass().add("no-background");
                }
            });
        });

        var dateString = e.getDate().toDisplayString(AppI18n.activeLanguage().getValue().getLocale());
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
            var name = new TextField();
            name.setMinWidth(50);
            name.setPrefWidth(50);
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.setAlignment(Pos.CENTER);
            name.setText(e.getName().equals(dateString) ? "" : e.getName());
            name.setAccessibleText("Custom savegame name");
            topBar.setCenter(name);
            BorderPane.setMargin(name, new Insets(3, 10, 3, 10));

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

    private void setupDragAndDrop(Region r, SavegameEntry<T, I> e) {
        r.setOnDragDetected(me -> {
            if (FILE_ICON == null) {
                var url = AppResources.getResourceURL(AppResources.MAIN_MODULE, "img/graphics/file_icon.png");
                FILE_ICON = new Image(url.orElseThrow().toString(), 80, 80, true, false);
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

    private HBox createButtonBar(SavegameEntry<T, I> e) {
        HBox staticButtons = new HBox();
        staticButtons.setAlignment(Pos.CENTER);
        staticButtons.getStyleClass().add(CLASS_BUTTON_BAR);
        {
            Button export = new Button(null, new FontIcon());
            export.setGraphic(new FontIcon());
            export.setOnMouseClicked((m) -> {
                SavegameActions.exportSavegame(e);
                savegameManagerState.selectEntry(null);
            });
            export.getStyleClass().add(CLASS_EXPORT);
            export.setAccessibleText("Export");
            GuiTooltips.install(export, AppI18n.get("exportSavegame", SavegameContext.getContext(e).getGame().getTranslatedFullName()));
            staticButtons.getChildren().add(export);
        }
        {
            Button copy = new Button(null, new FontIcon());
            copy.setGraphic(new FontIcon());
            copy.setOnMouseClicked((m) -> {
                SavegameActions.copySavegame(e);
            });
            copy.getStyleClass().add(CLASS_COPY);
            copy.setAccessibleText("Copy");
            GuiTooltips.install(copy, AppI18n.get("copySavegame"));
            staticButtons.getChildren().add(copy);
        }
        {
            Button notes = new Button(null, new FontIcon());
            notes.setGraphic(new FontIcon("mdi2n-note-text-outline"));
            notes.setOnMouseClicked((m) -> {
                GuiSavegameNotes.showSavegameNotesDialog(e.getNotes());
            });
            notes.setAccessibleText("Notes");
            GuiTooltips.install(notes, AppI18n.get("editSavegameNotes"));
            staticButtons.getChildren().add(notes);
        }

        {
            Button del = new Button(null, new FontIcon());
            del.setGraphic(new FontIcon("mdi2t-trash-can-outline"));
            del.setOnMouseClicked((m) -> {
                var confirm = AppDialog.confirm("deleteSavegame");
                if (confirm) {
                    SavegameActions.delete(e);
                }
            });
            del.setAccessibleText("Delete");
            GuiTooltips.install(del, AppI18n.get("deleteSavegame"));
            staticButtons.getChildren().add(del);
        }

        Button open = new Button(null, new FontIcon());
        open.setGraphic(new FontIcon("mdi2c-content-save-outline"));
        GuiTooltips.install(open, AppI18n.get("openSavegame"));
        staticButtons.getChildren().add(open);
        open.setOnMouseClicked((m) -> {
            SavegameActions.openSavegame(e);
        });


        HBox dynamicButtons = new HBox();
        dynamicButtons.setAlignment(Pos.CENTER);
        dynamicButtons.getStyleClass().add(CLASS_BUTTON_BAR);


        {
            Button melt = new Button(null, new FontIcon());
            melt.setGraphic(new FontIcon());
            melt.setOnMouseClicked((m) -> {
                SavegameActions.meltSavegame(e);
            });
            melt.getStyleClass().add(CLASS_MELT);
            melt.setAccessibleText("Melt");
            GuiTooltips.install(melt, AppI18n.get("meltSavegame"));
            SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
                if (ctx.getInfo().getData().isBinary() && RakalyHelper.shouldShowButton(ctx)) {
                    Platform.runLater(() -> {
                        dynamicButtons.getChildren().add(melt);
                    });
                }
            });
        }

        SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
            if (!SavegameBranches.supportsBranching(e)) {
                return;
            }

            Platform.runLater(() -> {
                Button branch = new Button(null, new FontIcon());
                branch.setGraphic(new FontIcon());
                branch.setOnMouseClicked((m) -> {
                    SavegameActions.branch(e);
                });
                branch.getStyleClass().add("branch-button");
                branch.setAccessibleText("Branch");
                GuiTooltips.install(branch, AppI18n.get("branchSavegame"));
                dynamicButtons.getChildren().addFirst(branch);
            });
        });

        if (OsType.ofLocal() == OsType.WINDOWS) {
            SavegameContext.withSavegameInfoContextAsync(e, ctx -> {
                ConverterSupport.ALL.forEach(converterSupport -> {
                    if (converterSupport.getFromGame().equals(ctx.getGame()) && GameInstallation.ALL.get(converterSupport.getToGame()) != null) {
                        Platform.runLater(() -> {
                            Button convert = new Button(null, new FontIcon());
                            convert.setGraphic(new FontIcon());
                            convert.setOnMouseClicked((m) -> {
                                converterSupport.convert(e);
                            });
                            convert.getStyleClass().add(CLASS_CONVERT);
                            convert.setAccessibleText("Convert to " + converterSupport.getToName() + " savegame");
                            GuiTooltips.install(convert, AppI18n.get("convertTo" + converterSupport.getToName()));
                            dynamicButtons.getChildren().add(convert);
                        });
                    }
                });
            });
        }

        Button edit = new Button(null, new FontIcon());
        edit.setOnMouseClicked((m) -> {
            SavegameActions.editSavegame(e);
        });
        edit.setAccessibleText("Edit");
        GuiTooltips.install(edit, AppI18n.get("editSavegame"));
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
                        dynamicButtons.getChildren().addFirst(edit);
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
        buttonBar.setSpacing(30);
        buttonBar.setAlignment(Pos.CENTER);
        return buttonBar;
    }

    private JFXMasonryPane createEmptyContainer() {
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

    private Node createSavegameInfoNode(SavegameEntry<T, I> entry) {
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

        entry.stateProperty().addListener(new ChangeListener<>() {
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
                    savegameManagerState.loadEntryAsync(entry);
                    stack.sceneProperty().removeListener(this);
                }
            }
        });

        return stack;
    }

    @Override
    protected Region createSimple() {
        VBox entryNode = new VBox();
        entryNode.setAlignment(Pos.CENTER);
        entryNode.setFillWidth(true);
        entryNode.getStyleClass().add(CLASS_ENTRY);
        entryNode.setOnMouseClicked(event -> savegameManagerState.selectEntry(e));
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

        // Somehow, the vertical padding on list cells does not apply
        // So do this instead
        var box = new VBox(entryNode, new Spacer(8, Orientation.VERTICAL));
        return box;
    }
}
