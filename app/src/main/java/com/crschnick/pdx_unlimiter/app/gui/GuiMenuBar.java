package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4SavegameImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.jfoenix.controls.*;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

public class GuiMenuBar {

    private static MenuBar createMenuBar() {
        Menu menu = new Menu("File");

        MenuItem menuItem0 = new MenuItem("Export savegames...");
        menuItem0.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showExportDialog(false);
            path.ifPresent(SavegameCache::exportSavegameDirectory);
        });


        MenuItem l = new MenuItem("Import latest savegame (CTRL+SHIFT+I)");
        l.setOnAction((a) -> {
            Eu4SavegameImporter.importLatestSavegame();

        });
        menu.getItems().add(l);

        MenuItem i = new MenuItem("Import all savegames...");
        i.setOnAction((a) -> {
            if (DialogHelper.showImportSavegamesDialog()) {
                Eu4SavegameImporter.importAllSavegames();
            }
        });
        menu.getItems().add(i);

        menu.getItems().add(menuItem0);


        Menu settings = new Menu("Settings");
        MenuItem c = new MenuItem("Change settings");
        c.setOnAction((a) -> {
            GuiSettings.showSettings();
        });
        settings.getItems().add(c);


        Menu savegames = new Menu("Storage");

        MenuItem menuItem1 = new MenuItem("Import storage...");
        menuItem1.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showImportArchiveDialog();
            path.ifPresent(SavegameCache::importSavegameCache);
        });
        savegames.getItems().add(menuItem1);

        MenuItem menuItem2 = new MenuItem("Export storage...");
        menuItem2.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showExportDialog(true);
            path.ifPresent(SavegameCache::exportSavegameCache);
        });
        savegames.getItems().add(menuItem2);

        MenuItem u = new MenuItem("Update all savegames...");
        u.setOnAction((a) -> {
            if (DialogHelper.showUpdateAllSavegamesDialog()) {
                //SavegameCache.EU4_CACHE.updateAllData();
            }
        });
        savegames.getItems().add(u);

        MenuItem sd = new MenuItem("Open storage directory");
        sd.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(GameIntegration.current().getSavegameCache().getPath().toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
        savegames.getItems().add(sd);

        MenuItem backups = new MenuItem("Open backup location");
        backups.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(GameIntegration.current().getSavegameCache().getBackupPath().toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
        savegames.getItems().add(backups);


        Menu about = new Menu("Help");
        MenuItem wiki = new MenuItem("EU4 Wiki");
        wiki.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://eu4.paradoxwikis.com/Europa_Universalis_4_Wiki"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem src = new MenuItem("Contribute");
        src.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/crschnick/pdx_unlimiter/"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem is = new MenuItem("Report an issue");
        is.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/crschnick/pdx_unlimiter/issues"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem lc = new MenuItem("License");
        lc.setOnAction((a) -> {
            DialogHelper.showText("License", "License", "license.txt");
        });
        MenuItem tc = new MenuItem("Third party software");
        tc.setOnAction((a) -> {
            DialogHelper.showText("Third party information", "A list of all software used to create the Pdx-Unlimiter", "third_party.txt");
        });
        about.getItems().add(wiki);
        about.getItems().add(src);
        about.getItems().add(is);
        about.getItems().add(lc);
        about.getItems().add(tc);


        Menu dev = new Menu("Developer");
        MenuItem ns = new MenuItem("Namespace creator");
        ns.setOnAction((a) -> {
            DialogHelper.createNamespaceDialog();
        });
        dev.getItems().add(ns);

        MenuItem log = new MenuItem("Show log");
        log.setOnAction((a) -> {
            DialogHelper.showLogDialog();
        });
        dev.getItems().add(log);


        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(menu);
        menuBar.getMenus().add(settings);
        menuBar.getMenus().add(savegames);
        menuBar.getMenus().add(about);
        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            menuBar.getMenus().add(dev);
        }
        return menuBar;
    }

    private static Node createStatusIndicator() {
        JFXButton m = new JFXButton("Switch game", new FontIcon());
        m.setOnAction(a -> GuiGameSwitcher.showGameSwitchDialog());
        return m;
    }

    public static Node createMenu() {
        MenuBar leftBar = createMenuBar();

        Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        HBox menubars = new HBox(leftBar, spacer, createStatusIndicator());
        return menubars;
    }
}
