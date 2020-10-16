package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;

public class SavegameManagerApp extends Application {

    private static SavegameManagerApp APP;

    public static SavegameManagerApp getAPP() {
        return APP;
    }

    private void createStatusThread(BorderPane layout) {
        Consumer<Eu4Campaign.Entry> launch = (e) -> {
            Path srcPath = SavegameCache.EU4_CACHE.getPath(e).resolve("savegame.eu4");
            Path destPath = Installation.EU4.get().getSaveDirectory().resolve("savegame.eu4");
            try {
                FileUtils.copyFile(srcPath.toFile(), destPath.toFile());
                Installation.EU4.get().writeLaunchConfig(e, destPath.relativize(Installation.EU4.get().getUserDirectory()));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            Installation.EU4.get().start();
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

    private BorderPane layout;

    private SimpleObjectProperty<Optional<Eu4Campaign>> selectedCampaign = new SimpleObjectProperty<>(Optional.empty());

    private SimpleObjectProperty<Optional<Eu4Campaign.Entry>> selectedSave = new SimpleObjectProperty<>(Optional.empty());

    private BooleanProperty running = new SimpleBooleanProperty(true);

    private void createLayout() {
        layout = new BorderPane();

        createStatusThread(layout);

        layout.setTop(Eu4SavegameManagerStyle.createMenu());
        layout.setLeft(Eu4SavegameManagerStyle.createCampaignList(SavegameCache.EU4_CACHE.getCampaigns(), selectedCampaign,
                (c) -> {
                    if (selectedCampaign.get().isPresent() && selectedCampaign.get().get().equals(c)) {
                        selectedCampaign.set(Optional.empty());

                        if (selectedSave.get().isPresent()&&
                                selectedCampaign.get().get().getSavegames().contains(selectedSave.get().get())) {
                            selectedSave.set(Optional.empty());
                        }
                    }


                    SavegameCache.EU4_CACHE.delete(c);
                }));
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
    }

    public void save() {
        try {
            SavegameCache.saveData();
            Installation.saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e, false);
        }
    }

    private void startSetup() {
        try {
            Installation.loadConfig();
            SavegameCache.loadData();
        } catch (Exception e) {
            ErrorHandler.handleException(e, true);
        }

        if (!Installation.isConfigured()) {
            DialogHelper.showSettings();
        }
    }

    public void close(boolean save) {
        if (save) {
            save();
        }
        running.setValue(false);
        Platform.exit();
    }

    @Override
    public void start(Stage primaryStage) {
        APP = this;

        startSetup();

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

    public static void main(String[] args) {
        launch(args);
    }

    public boolean isRunning() {
        return running.get();
    }

    public BooleanProperty runningProperty() {
        return running;
    }
}
