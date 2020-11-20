package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.*;
import com.crschnick.pdx_unlimiter.app.installation.*;
import com.crschnick.pdx_unlimiter.app.game.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jnativehook.GlobalScreen;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static PdxuApp APP;
    private Image icon;
    private BorderPane layout = new BorderPane();
    private BooleanProperty running = new SimpleBooleanProperty(true);

    public static PdxuApp getApp() {
        return APP;
    }

    public static void main(String[] args) {
        try {
            launch(args);
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }
    }

    public Scene getScene() {
        return layout.getScene();
    }


    private void setCampainList(BorderPane layout) {
        layout.setLeft(GuiGameCampaignList.createCampaignList());
    }

    private void createLayout() {
        SavegameCache.EU4_CACHE.getCampaigns().addListener((SetChangeListener<? super Eu4Campaign>) (change) -> {
            Platform.runLater(() -> {
                if (change.getSet().size() == 1 && change.wasAdded()) {
                    createLayout();
                }
            });
        });

        layout.setTop(GuiMenuBar.createMenu());
        Pane p = new Pane();
        layout.setBottom(p);
        GuiStatusBar.createStatusBar(p);
        if (SavegameCache.EU4_CACHE.getCampaigns().size() == 0) {
            layout.setCenter(GuiGameCampaignList.createNoCampaignNode());
        } else {
            layout.setCenter(GuiGameCampaignEntryList.createCampaignEntryList());
            setCampainList(layout);
        }


        layout.setOnDragOver(event -> {
                if (event.getGestureSource() != layout
                        && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

        layout.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            for (File f : db.getFiles()) {
                if (FileImporter.importFile(f.toPath())) {
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void save() {
        try {
            SavegameCache.saveData();
            Settings.saveConfig();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
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
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    private void setup() throws Exception {
        if (!PdxuInstallation.init()) {
            System.exit(1);
        }
        LogManager.init();
        ErrorHandler.init();
        Settings.init();
        if (Settings.getInstance().getEu4().isEmpty()) {
            if (!DialogHelper.showInitialSettings()) {
                System.exit(1);
            }
        }

        GameInstallation.initInstallations();
        GameManager.init();
        SavegameCache.loadData();
        AchievementManager.init();
        if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
            GlobalScreen.registerNativeHook();
        }

        GameImage.loadImages();
    }

    @Override
    public void start(Stage primaryStage) {
        APP = this;
        icon = new Image(PdxuApp.class.getResourceAsStream("logo.png"));
        primaryStage.getIcons().add(icon);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Stage.getWindows().stream()
                        .filter(w -> !w.equals(getScene().getWindow()))
                        .collect(Collectors.toList())
                        .forEach(w -> w.fireEvent(event));

                close(true);
            }
        });
        primaryStage.setTitle("EU4 Savegame Manager");
        try {
            setup();
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }

        layout.styleProperty().setValue("-fx-font-size: 12pt; -fx-text-fill: white;");
        createLayout();
        GameIntegration.init();

        Scene scene = new Scene(layout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
        //setUserAgentStylesheet(STYLESHEET_CASPIAN);
        //primaryStage.getScene().getStylesheets().clear();
        GuiStyle.addStylesheets(primaryStage.getScene());


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
