package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.achievement.JsonPathConfiguration;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.savegame_mgr.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;
import org.jnativehook.GlobalScreen;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SavegameManagerApp extends Application {

    private Image icon;
    private static SavegameManagerApp APP;
    private BorderPane layout = new BorderPane();
    private SimpleObjectProperty<Optional<Eu4Campaign>> selectedCampaign = new SimpleObjectProperty<>(Optional.empty());
    private SimpleObjectProperty<Optional<Eu4Campaign.Entry>> selectedSave = new SimpleObjectProperty<>(Optional.empty());
    private BooleanProperty running = new SimpleBooleanProperty(true);

    public static SavegameManagerApp getAPP() {
        return APP;
    }

    public static void main(String[] args) {
        try {
            if (!PdxuInstallation.init()) {
                return;
            }
            Settings.init();
            GameInstallation.initInstallations();
            SavegameCache.loadData();

            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.SEVERE);
            logger.setUseParentHandlers(false);

            if (PdxuInstallation.getInstance().isProduction()) {
                GlobalScreen.registerNativeHook();
            }

            launch(args);
        } catch (Exception e) {
            ErrorHandler.handleExcetionWithoutPlatform(e);
        }
    }

    private void createStatusThread(BorderPane layout) {
        Consumer<Eu4Campaign.Entry> launch = (e) -> {
            Path srcPath = SavegameCache.EU4_CACHE.getPath(e).resolve("savegame.eu4");
            Path destPath = GameInstallation.EU4.getSaveDirectory().resolve("savegame.eu4");
            try {
                FileUtils.copyFile(srcPath.toFile(), destPath.toFile(), false);
                destPath.toFile().setLastModified(System.currentTimeMillis());
                GameInstallation.EU4.writeLaunchConfig(e, GameInstallation.EU4.getUserDirectory().relativize(destPath));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            GameInstallation.EU4.start();
            selectedCampaign.get().get().lastPlayedProperty().setValue(Timestamp.from(Instant.now()));

        };

        Thread t = new Thread(() -> {
            Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createInactiveStatusBar(selectedCampaign, selectedSave, launch)));

            Optional<PdxApp> oldApp = Optional.empty();
            while (true) {

                if (!oldApp.equals(PdxApp.getActiveApp())) {
                    var app = PdxApp.getActiveApp();
                    if (app.isPresent()) {
                        Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createActiveStatusBar(app.get())));
                    } else {
                        Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createInactiveStatusBar(selectedCampaign, selectedSave, launch)));
                    }
                    oldApp = app;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void setCampainList(BorderPane layout) {
        layout.setLeft(Eu4SavegameManagerStyle.createCampaignList(SavegameCache.EU4_CACHE.getCampaigns(), selectedCampaign,
                (c) -> {
                    if (selectedCampaign.get().isPresent() && selectedCampaign.get().get().equals(c)) {
                        selectedCampaign.set(Optional.empty());

                        if (selectedSave.get().isPresent() &&
                                selectedCampaign.get().get().getSavegames().contains(selectedSave.get().get())) {
                            selectedSave.set(Optional.empty());
                        }
                    }


                    SavegameCache.EU4_CACHE.delete(c);
                }));
    }

    private void createLayout() {
        SavegameCache.EU4_CACHE.getCampaigns().addListener((SetChangeListener<? super Eu4Campaign>) (change) -> {
            Platform.runLater(() -> {
                if (change.getSet().size() == 1 && change.wasAdded()) {
                    createLayout();
                }
            });
        });

        layout.setTop(Eu4SavegameManagerStyle.createMenu());
        createStatusThread(layout);
        if (SavegameCache.EU4_CACHE.getCampaigns().size() == 0) {
            layout.setCenter(Eu4SavegameManagerStyle.createNoCampaignNode());
        } else {
            layout.setCenter(Eu4SavegameManagerStyle.createSavegameScrollPane(selectedCampaign, selectedSave,
                    (e) -> {
                        try {
                            Desktop.getDesktop().open(SavegameCache.EU4_CACHE.getPath(e).toFile());
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    },
                    (e) -> {
                        if (selectedSave.get().isPresent() && selectedSave.get().get().equals(e)) {
                            selectedSave.set(Optional.empty());
                        }
                        SavegameCache.EU4_CACHE.delete(e);
                    }));

            selectedCampaign.addListener((c, o, n) -> {
                if (n.isPresent()) {
                    SavegameCache.EU4_CACHE.loadAsync(n.get());
                }
            });
            setCampainList(layout);
        }
    }

    public void save() {
        try {
            SavegameCache.saveData();
            Settings.saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
        }
    }

    public void close(boolean save) {
        if (save) {
            save();
        }
        running.setValue(false);
        Platform.exit();
        try {
            PdxuInstallation.shutdown();
            GlobalScreen.unregisterNativeHook();
        } catch (Exception e) {
            ErrorHandler.handleExcetionWithoutPlatform(e);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        APP = this;
        icon = new Image(SavegameManagerApp.class.getResourceAsStream("logo.png"));
        primaryStage.getIcons().add(icon);

        AchievementManager.init();

        if (Settings.getInstance().getEu4().isEmpty()) {
            if (!DialogHelper.showInitialSettings()) {
                System.exit(0);
            } else {
                try {
                    GameInstallation.initInstallations();
                } catch (Exception e) {
                    ErrorHandler.handleException(e, true);
                }
            }
        }

        createLayout();

        primaryStage.setTitle("EU4 Savegame Manager");
        Scene scene = new Scene(layout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                close(true);
            }
        });
    }

    public Image getIcon() {
        return icon;
    }

    public boolean isRunning() {
        return running.get();
    }

    public BooleanProperty runningProperty() {
        return running;
    }
}
