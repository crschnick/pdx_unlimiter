package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppFontSizes;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.gui.game.GameGuiFactory;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameAppManager;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;
import com.crschnick.pdxu.app.savegame.*;
import com.crschnick.pdxu.app.util.Hyperlinks;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import lombok.RequiredArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

@RequiredArgsConstructor
public class GuiStatusBarComp<T, I extends SavegameInfo<T>> extends SimpleComp {

    private enum Status {
        NONE,
        SELECTED,
        RUNNING
    }

    private final SavegameManagerState<T, I> savegameManagerState;
    private Status status;

    private Region createRunningBar(Game g) {
        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);
        barPane.getStyleClass().add(CLASS_STATUS_RUNNING);

        Label text = new Label(g.getTranslatedFullName() + " (" + AppI18n.get("running") + ")",
                GameGuiFactory.get(g).createIcon());
        text.getStyleClass().add(CLASS_TEXT);
        barPane.setLeft(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        Label latest = new Label();
        latest.setGraphic(new FontIcon());
        latest.getStyleClass().add(CLASS_TEXT);
        latest.getStyleClass().add(CLASS_SAVEGAME);

        var watcher = SavegameWatcher.ALL.get(g);
        javafx.beans.value.ChangeListener<List<FileImportTarget.StandardImportTarget>> l = (c, o, n) -> {
            Platform.runLater(() -> {
                var name = watcher.getLatest()
                        .map(FileImportTarget.StandardImportTarget::getName)
                        .orElse(AppI18n.get("none"));
                latest.setText(AppI18n.get("latest") + ": " + name);
            });
        };
        watcher.savegamesProperty().addListener(l);
        l.changed(null, null, watcher.savegamesProperty().get());
        barPane.setCenter(latest);

        Button importLatest = new Button(AppI18n.get("import"));
        importLatest.setGraphic(new FontIcon());
        importLatest.getStyleClass().add(CLASS_IMPORT);
        importLatest.setOnAction(event -> {
            GameAppManager.getInstance().importLatest();
            event.consume();
        });

        Button b = new Button(AppI18n.get("kill"));
        b.setGraphic(new FontIcon());
        b.getStyleClass().add(CLASS_KILL);
        b.setOnAction(event -> {
            GameAppManager.getInstance().kill();
            event.consume();
        });

        HBox buttons = new HBox(importLatest);
        buttons.setSpacing(5);
        buttons.getChildren().add(b);

        {
            Button help = new Button();
            help.setGraphic(new FontIcon());
            help.getStyleClass().add("help-button");
            help.setOnAction(event -> {
                Hyperlinks.open(Hyperlinks.SAVESCUM_GUIDE);
                event.consume();
            });
            buttons.getChildren().add(help);
        }

        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);

        barPane.setRight(buttons);
        return barPane;
    }

    private Region createEntryStatusBar(SavegameEntry<T, I> e) {
        BorderPane barPane = new BorderPane();
        barPane.getStyleClass().add(CLASS_STATUS_BAR);

        HBox buttons = new HBox();
        buttons.setSpacing(10);
        buttons.setFillHeight(true);
        buttons.setAlignment(Pos.CENTER);
        barPane.setRight(buttons);

        SavegameContext.withSavegameContext(e, ctx -> {
            Label text = new Label(
                    ctx.getStorage().getEntryName(e),
                    ctx.getGuiFactory().createIcon());
            AppFontSizes.xl(text);
            text.setGraphicTextGap(9);
            text.getStyleClass().add("text");
            barPane.setLeft(text);
            BorderPane.setAlignment(text, Pos.CENTER);

            var externalData = SavegameCompatibility.determineForModsAndDLCs(e);
            var version = SavegameCompatibility.determineForVersion(ctx.getGame(), ctx.getInfo().getData().getVersion());
            if (externalData == SavegameCompatibility.Compatbility.INCOMPATIBLE || version == SavegameCompatibility.Compatbility.INCOMPATIBLE) {
                barPane.getStyleClass().add(CLASS_STATUS_INCOMPATIBLE);
            } else if (externalData == SavegameCompatibility.Compatbility.UNKNOWN || version == SavegameCompatibility.Compatbility.UNKNOWN) {
                barPane.getStyleClass().add("status-compatible-unknown");
            }
            {
                Button export = new Button(AppI18n.get("export"));
                export.setGraphic(new FontIcon());
                export.getStyleClass().add(CLASS_EXPORT);
                export.setOnAction(event -> {
                    SavegameActions.exportSavegame(e);
                    savegameManagerState.selectEntry(null);

                    event.consume();
                    hide(barPane);
                });
                buttons.getChildren().add(export);
            }

            if (e.getInfo() != null && ctx.getInstallation().getDist().supportsLauncher()) {
                Button launch = new Button(AppI18n.get("startLauncher"));
                launch.setGraphic(new FontIcon());
                launch.getStyleClass().add("launcher-button");
                launch.setOnAction(event -> {
                    GameDistLauncher.startLauncherWithContinueGame(e);
                    savegameManagerState.selectEntry(null);

                    event.consume();
                    hide(barPane);
                });
                buttons.getChildren().add(launch);
            }

            if (e.getInfo() != null && ctx.getInstallation().getDist().supportsDirectLaunch()) {
                ButtonBase launch;
                if (ctx.getInstallation().getType().debugModeSwitch().isPresent()) {
                    var splitButton = new SplitMenuButton();
                    splitButton.setText(AppI18n.get("continueGame"));

                    var debugItem = new MenuItem(AppI18n.get("debugMode"));
                    debugItem.setGraphic(new FontIcon());
                    debugItem.getStyleClass().add("continue-button");
                    debugItem.setOnAction(event -> {
                        GameDistLauncher.continueSavegame(e, true);
                        savegameManagerState.selectEntry(null);
                        event.consume();
                        hide(barPane);
                    });
                    splitButton.getItems().add(debugItem);
                    launch = splitButton;
                } else {
                    launch = new Button(AppI18n.get("continueGame"));
                }

                launch.setGraphic(new FontIcon());
                launch.getStyleClass().add("continue-button");
                launch.setOnAction(event -> {
                    GameDistLauncher.continueSavegame(e, false);
                    savegameManagerState.selectEntry(null);
                    event.consume();
                    hide(barPane);
                });
                buttons.getChildren().add(launch);
            }

            {
                Button help = new Button();
                help.setGraphic(new FontIcon());
                help.getStyleClass().add("help-button");
                help.setOnAction(event -> {
                    Hyperlinks.open(Hyperlinks.LAUNCH_GUIDE);
                    event.consume();
                });
                buttons.getChildren().add(help);
            }
        });

        return barPane;
    }

    private void show(Pane pane, Region bar) {
        Platform.runLater(() -> {
            pane.getChildren().setAll(bar);
            bar.prefWidthProperty().bind(pane.widthProperty());
        });
    }

    private void hide(Pane pane) {
        Platform.runLater(() -> {
            pane.getChildren().clear();
        });
    }

    private void select(Pane pane, SavegameEntry<T, I> e) {
        Region bar = createEntryStatusBar(e);
        Platform.runLater(() -> {
            if (status == Status.RUNNING) {
                return;
            }

            status = Status.SELECTED;
            show(pane, bar);
        });
    }

    private void unselect(Pane pane) {
        Platform.runLater(() -> {
            if (status == Status.RUNNING) {
                return;
            }

            hide(pane);
            status = Status.NONE;
        });
    }

    private void setRunning(Pane pane, Game g) {
        // Create node before Platform thread to avoid async issues!
        Region bar = createRunningBar(g);
        Platform.runLater(() -> {
            show(pane, bar);
            status = Status.RUNNING;
        });
    }

    private void stopRunning(Pane pane) {
        hide(pane);
        status = Status.NONE;
        var sel = savegameManagerState.globalSelectedEntryProperty().get();
        if (sel != null) {
            select(pane, sel);
        }
    }


    @Override
    protected Region createSimple() {
        Pane pane = new Pane();
        savegameManagerState.globalSelectedEntryProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                if (n != null) {
                    select(pane, n);
                } else {
                    unselect(pane);
                }
            });
        });

        GameAppManager.getInstance().activeGameProperty().addListener((c, o, n) -> {
            if (n != null) {
                setRunning(pane, n.getGame());
            } else {
                stopRunning(pane);
            }
        });
        var g = GameAppManager.getInstance().activeGameProperty().get();
        if (g != null) {
            setRunning(pane, g.getGame());
        }

        return pane;
    }
}
