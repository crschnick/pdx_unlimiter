package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.gui.dialog.*;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.SavegameStorageIO;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.MemoryHelper;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.Optional;

public class GuiMenuBar {

    private static MenuBar createMenuBar() {

        Menu pdxu = new Menu("Pdx-Unlimiter");

        if (PdxuInstallation.getInstance().isStandalone()) {
            MenuItem lr = new MenuItem(PdxuI18n.get("CHECK_UPDATE"));
            lr.setOnAction((a) -> {
                ThreadHelper.browse(Hyperlinks.RELEASES);
            });
            pdxu.getItems().add(lr);
        }

        MenuItem ed = new MenuItem(PdxuI18n.get("OPEN_EDITOR"));
        ed.setOnAction((a) -> {
            EditorProvider.get().browseExternalFile();
        });
        pdxu.getItems().add(ed);

        MenuItem c = new MenuItem(PdxuI18n.get("CHANGE_SETTINGS"));
        c.setOnAction((a) -> {
            GuiSettings.showSettings();
        });
        pdxu.getItems().add(c);

        MenuItem rel = new MenuItem(PdxuI18n.get("RELOAD"));
        rel.setOnAction((a) -> {
            ComponentManager.reloadSettings(() -> {
            });
        });
        pdxu.getItems().add(rel);

        MenuItem export = new MenuItem(PdxuI18n.get("EXPORT_STORAGE"));
        export.setOnAction((a) -> {
            Optional<Path> path = GuiSavegameIO.showExportDialog();
            path.ifPresent(p -> {
                SavegameStorageIO.exportSavegameStorage(p);
            });
        });
        pdxu.getItems().add(export);


        Menu about = new Menu(PdxuI18n.get("ABOUT"));

        MenuItem other = new MenuItem(PdxuI18n.get("OTHER_PROJECTS"));
        other.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.XPIPE);
        });

        MenuItem tri = new MenuItem(PdxuI18n.get("TRANSLATE"));
        tri.setOnAction((a) -> {
            GuiTranslate.showTranslatationAlert();
        });

        MenuItem src = new MenuItem(PdxuI18n.get("CONTRIBUTE"));
        src.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.MAIN_PAGE);
        });

        MenuItem lc = new MenuItem(PdxuI18n.get("LICENSE"));
        lc.setOnAction((a) -> {
            GuiDialogHelper.showText(PdxuI18n.get("LICENSE"), PdxuI18n.get("LICENSE"), "license.txt");
        });
        MenuItem tc = new MenuItem(PdxuI18n.get("THIRD_PARTY"));
        tc.setOnAction((a) -> {
            GuiDialogHelper.showText(
                    PdxuI18n.get("THIRD_PARTY_TITLE"),
                    PdxuI18n.get("THIRD_PARTY_INFO"),
                    "third_party.txt");
        });
        about.getItems().add(other);
        about.getItems().add(tri);
        about.getItems().add(src);
        about.getItems().add(lc);
        about.getItems().add(tc);


        Menu help = new Menu(PdxuI18n.get("HELP"));

        MenuItem guide = new MenuItem(PdxuI18n.get("USER_GUIDE"));
        guide.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.GUIDE);
        });
        help.getItems().add(guide);

        MenuItem is = new MenuItem(PdxuI18n.get("REPORT_ISSUE"));
        is.setOnAction((a) -> {
            ErrorHandler.reportIssue(null);
        });
        help.getItems().add(is);

        MenuItem discord = new MenuItem(PdxuI18n.get("DISCORD"));
        discord.setOnAction((a) -> {
            Hyperlinks.open(Hyperlinks.DISCORD);
        });
        help.getItems().add(discord);

        MenuItem log = new MenuItem(PdxuI18n.get("SHOW_LOG"));
        log.setOnAction((a) -> {
            GuiLog.showLogDialog();
        });
        help.getItems().add(log);


        Menu dev = new Menu("Developer");

        MenuItem error = new MenuItem("Throw exception");
        error.setOnAction((a) -> {
            throw new RuntimeException("This is a test exception!");
        });
        dev.getItems().add(error);

        MenuItem terminalError = new MenuItem("Throw terminal exception");
        terminalError.setOnAction((a) -> {
            ErrorHandler.handleTerminalException(new RuntimeException("This is a terminal test exception!"));
        });
        dev.getItems().add(terminalError);

        MenuItem gc = new MenuItem("Force GC");
        gc.setOnAction((a) -> {
            MemoryHelper.forceGC();
        });
        dev.getItems().add(gc);


        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(false);
        menuBar.getMenus().add(pdxu);
        menuBar.getMenus().add(about);
        menuBar.getMenus().add(help);
        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            menuBar.getMenus().add(dev);
        }
        return menuBar;
    }

    private static Node createRightBar() {
        JFXButton m = new JFXButton(PdxuI18n.get("SWITCH_GAME"));
        m.setGraphic(new FontIcon());
        m.getStyleClass().add(GuiStyle.CLASS_SWTICH_GAME);
        m.setOnAction(a -> GuiGameSwitcher.showGameSwitchDialog());
        m.setDisable(SavegameManagerState.get().current() == null);
        SavegameManagerState.get().onGameChange(n -> {
            Platform.runLater(() -> m.setDisable(n == null));
        });

        JFXButton importB = new JFXButton(PdxuI18n.get("IMPORT"));
        importB.setOnAction(e -> {
            GuiImporter.createImporterDialog();
            e.consume();
        });
        importB.setGraphic(new FontIcon());
        importB.getStyleClass().add(GuiStyle.CLASS_IMPORT);
        importB.setDisable(SavegameManagerState.get().current() == null);
        SavegameManagerState.get().onGameChange(n -> {
            Platform.runLater(() -> importB.setDisable(n == null));
        });

        JFXButton launch = new JFXButton(PdxuI18n.get("LAUNCH"));
        launch.setOnAction(e -> {
            GameDistLauncher.startLauncher();
            e.consume();
        });
        launch.setGraphic(new FontIcon());
        launch.getStyleClass().add(GuiStyle.CLASS_LAUNCH);
        launch.setDisable(SavegameManagerState.get().current() == null);
        SavegameManagerState.get().onGameChange(n -> {
            Platform.runLater(() -> {
                boolean disable = n == null || !GameInstallation.ALL.get(n).getDist().supportsLauncher();
                launch.setDisable(disable);
            });
        });

        var box = new HBox(m, importB, launch);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public static Node createMenu() {
        MenuBar leftBar = createMenuBar();

        StackPane spacer = new StackPane();
        Label game = new Label();
        SavegameManagerState.get().onGameChange(n -> {
            String name = n != null ? n.getTranslatedFullName() : PdxuI18n.get("NO_GAME");
            Platform.runLater(() -> game.setText(name));
        });
        spacer.getChildren().add(game);
        spacer.setAlignment(Pos.CENTER);

        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        StackPane s = new StackPane();
        s.getStyleClass().add("menu-bar");
        s.getChildren().add(createRightBar());

        return new HBox(leftBar, spacer, s);
    }
}
