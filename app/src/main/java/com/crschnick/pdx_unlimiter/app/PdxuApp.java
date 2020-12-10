package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.game.GameAppManager;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.gui.GameImage;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.gui.GuiSettings;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.LogManager;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jnativehook.GlobalScreen;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static PdxuApp APP;
    private Image icon;
    private StackPane layout;
    private BooleanProperty running = new SimpleBooleanProperty(true);

    public static PdxuApp getApp() {
        return APP;
    }

    public static void main(String[] args) {
        try {
            initialSetup(args);
        } catch (Exception e) {
            ErrorHandler.handleTerminalException(e);
        }

        launch(args);
    }

    public StackPane getLayout() {
        return layout;
    }

    public Scene getScene() {
        return layout.getScene();
    }

    public void close() {
        running.setValue(false);
        SavegameCache.destroyCaches();

        Platform.exit();

        try {
            if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }

    private static void initialSetup(String[] args) throws Exception {
        PdxuInstallation.init();
        LogManager.init();
        ErrorHandler.init();

        LoggerFactory.getLogger(PdxuApp.class).info("Running pdxu with arguments: " + Arrays.toString(args));
        Arrays.stream(args)
                .map(Path::of)
                .forEach(FileImporter::addToImportQueue);

        if (!PdxuInstallation.shouldStart()) {
            System.exit(0);
        }
    }

    private void postWindowSetup() {
        new Thread(() -> {
            try {
                Settings.init();
                GameAppManager.init();
                FileImporter.init();

                if (PdxuInstallation.getInstance().isNativeHookEnabled()) {
                    GlobalScreen.registerNativeHook();
                }
            } catch (Exception e) {
                ErrorHandler.handleTerminalException(e);
            }
        }).start();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            APP = this;
            icon = new Image(PdxuApp.class.getResourceAsStream("logo.png"));
            primaryStage.getIcons().add(icon);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    close();

                    Stage.getWindows().stream()
                            .filter(w -> !w.equals(getScene().getWindow()))
                            .collect(Collectors.toList())
                            .forEach(w -> w.fireEvent(event));

                }
            });

            layout = GuiLayout.createLayout();

            primaryStage.setTitle("Pdx-Unlimiter (" + PdxuInstallation.getInstance().getVersion() + ")");
            Scene scene = new Scene(layout, 1000, 720);
            primaryStage.setScene(scene);
            primaryStage.show();
            GuiStyle.addStylesheets(primaryStage.getScene());

            postWindowSetup();
        } catch (Exception ex) {
            ErrorHandler.handleTerminalException(ex);
        }

    }

    public Image getIcon() {
        return icon;
    }

    public boolean isRunning() {
        return running.get();
    }
}
