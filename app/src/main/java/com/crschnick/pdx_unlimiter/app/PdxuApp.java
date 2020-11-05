package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.game.Eu4CampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import com.crschnick.pdx_unlimiter.app.gui.Eu4SavegameManagerStyle;
import com.crschnick.pdx_unlimiter.app.gui.GameImage;
import com.crschnick.pdx_unlimiter.app.gui.GuiStatusBar;
import com.crschnick.pdx_unlimiter.app.installation.*;
import com.crschnick.pdx_unlimiter.app.game.Eu4Campaign;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.jfoenix.controls.JFXSnackbar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.Optional;
import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static PdxuApp APP;
    private Image icon;
    private BorderPane layout = new BorderPane();
    private SimpleObjectProperty<Optional<Eu4Campaign>> selectedCampaign = new SimpleObjectProperty<>(Optional.empty());
    private SimpleObjectProperty<Optional<Eu4CampaignEntry>> selectedSave = new SimpleObjectProperty<>(Optional.empty());
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
        Pane p = new Pane();
        layout.setBottom(p);
        GuiStatusBar.createStatusBar(selectedCampaign, selectedSave, p);
        if (SavegameCache.EU4_CACHE.getCampaigns().size() == 0) {
            layout.setCenter(Eu4SavegameManagerStyle.createNoCampaignNode());
        } else {
            layout.setCenter(Eu4SavegameManagerStyle.createSavegameList(selectedCampaign, selectedSave));
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
        GameIntegration.init();
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

        createLayout();
        layout.styleProperty().setValue("-fx-font-size: 12pt; -fx-text-fill: white;");

        Scene scene = new Scene(layout, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
        //setUserAgentStylesheet(STYLESHEET_CASPIAN);
        //primaryStage.getScene().getStylesheets().clear();
        primaryStage.getScene().getStylesheets().add(
                PdxuApp.class.getResource("style.css").toExternalForm());
        primaryStage.getScene().getStylesheets().add(
                PdxuApp.class.getResource("scrollbar.css").toExternalForm());
        primaryStage.getScene().getStylesheets().add(
                PdxuApp.class.getResource("buttons.css").toExternalForm());


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
