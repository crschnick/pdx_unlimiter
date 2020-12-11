package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public class GuiMenuBar {

    private static MenuBar createMenuBar() {

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
            //path.ifPresent(SavegameCache::importSavegameCache);
        });
        savegames.getItems().add(menuItem1);

        MenuItem menuItem2 = new MenuItem("Export storage...");
        menuItem2.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showExportDialog(true);
            //path.ifPresent(SavegameCache::exportSavegameCache);
        });
        savegames.getItems().add(menuItem2);

        MenuItem exportSg = new MenuItem("Export savegames...");
        exportSg.setOnAction((a) -> {
            Optional<Path> path = DialogHelper.showExportDialog(false);
            //path.ifPresent(SavegameCache::exportSavegameDirectory);
        });
        savegames.getItems().add(exportSg);

        MenuItem sd = new MenuItem("Open storage directory");
        sd.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(GameIntegration.current().getSavegameCache().getPath().toFile());
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        });
        savegames.getItems().add(sd);

        Menu achievements = new Menu("Achievements");
        MenuItem asrc = new MenuItem("Achievement source");
        asrc.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/crschnick/pdxu_achievements/"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        MenuItem oa = new MenuItem("Official achievements directory");
        oa.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(PdxuInstallation.getInstance().getOfficialAchievementsLocation().toFile());
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });

        MenuItem ua = new MenuItem("User achievements directory");
        ua.setOnAction((a) -> {
            try {
                Desktop.getDesktop().open(PdxuInstallation.getInstance().getUserAchievementsLocation().toFile());
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });

        achievements.getItems().add(asrc);
        achievements.getItems().add(oa);
        achievements.getItems().add(ua);


        Menu about = new Menu("About");
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

        MenuItem error = new MenuItem("Throw exception");
        error.setOnAction((a) -> {
            throw new RuntimeException("This is a test exception!");
        });
        dev.getItems().add(error);


        MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().add(settings);
        menuBar.getMenus().add(savegames);
        menuBar.getMenus().add(achievements);
        menuBar.getMenus().add(about);
        if (PdxuInstallation.getInstance().isDeveloperMode()) {
            menuBar.getMenus().add(dev);
        }
        return menuBar;
    }

    private static Node createGameIndicator() {
        JFXButton m = new JFXButton();
        GameIntegration.currentGameProperty().addListener((c,o,n) -> {
            var current = GameIntegration.current();
            var name = current != null ? GameIntegration.current().getName() : "None";
            Platform.runLater(() -> m.setText(name));
        });
        m.setGraphic(new FontIcon());
        m.getStyleClass().add(GuiStyle.CLASS_SWTICH_GAME);
        m.setOnAction(a -> GuiGameSwitcher.showGameSwitchDialog());

        JFXButton importB = new JFXButton();
        importB.setOnAction(e -> {
            GuiImporter.createImporterDialog(GameIntegration.current().getInstallation());
            e.consume();
        });
        importB.setGraphic(new FontIcon());
        importB.getStyleClass().add(GuiStyle.CLASS_IMPORT);

        JFXButton launch = new JFXButton();
        launch.setOnAction(e -> {
            GameIntegration.current().getInstallation().startLauncher();
            e.consume();
        });
        launch.setGraphic(new FontIcon());
        launch.getStyleClass().add(GuiStyle.CLASS_LAUNCH);

        var box = new HBox(m, importB, launch);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    public static Node createMenu() {
        MenuBar leftBar = createMenuBar();

        Region spacer = new Region();
        spacer.getStyleClass().add("menu-bar");
        HBox.setHgrow(spacer, Priority.SOMETIMES);

        StackPane s = new StackPane();
        s.getStyleClass().add("menu-bar");
        s.getChildren().add(createGameIndicator());

        HBox menubars = new HBox(leftBar, spacer, s);
        return menubars;
    }
}
