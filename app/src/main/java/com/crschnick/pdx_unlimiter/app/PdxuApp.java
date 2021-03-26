package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.settings.SavedState;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(PdxuApp.class);

    private static PdxuApp APP;

    private GuiLayout layout;
    private Stage stage;
    private Image icon;
    private boolean windowActive;

    public static PdxuApp getApp() {
        return APP;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setupWindowState() {
        var w = stage;

        // Set size to default
        w.setWidth(1200);
        w.setHeight(700);

        var s = SavedState.getInstance();

        boolean inBounds = false;
        for (Screen screen : Screen.getScreens()) {
            Rectangle2D visualBounds = screen.getVisualBounds();
            // Check whether the bounds intersect where the intersection is larger than 20 pixels!
            if (visualBounds.intersects(new Rectangle2D(
                    s.getWindowX() + 20,
                    s.getWindowY() + 20,
                    s.getWindowWidth() - 40,
                    s.getWindowHeight() - 40))) {
                inBounds = true;
                break;
            }
        }
        if (inBounds) {
            if (s.getWindowX() != SavedState.INVALID) w.setX(s.getWindowX());
            if (s.getWindowY() != SavedState.INVALID) w.setY(s.getWindowY());
            if (s.getWindowWidth() != SavedState.INVALID) w.setWidth(s.getWindowWidth());
            if (s.getWindowHeight() != SavedState.INVALID) w.setHeight(s.getWindowHeight());
            if (s.isMaximized()) w.setMaximized(true);
        } else {
            logger.warn("Saved window was out of bounds");
        }

        stage.xProperty().addListener((c, o, n) -> {
            if (windowActive) {
                logger.debug("Changing window x to " + n.intValue());
                s.setWindowX(n.intValue());
            }
        });
        stage.yProperty().addListener((c, o, n) -> {
            if (windowActive) {
                logger.debug("Changing window y to " + n.intValue());
                s.setWindowY(n.intValue());
            }
        });
        stage.widthProperty().addListener((c, o, n) -> {
            if (windowActive) {
                logger.debug("Changing window width to " + n.intValue());
                s.setWindowWidth(n.intValue());
            }
        });
        stage.heightProperty().addListener((c, o, n) -> {
            if (windowActive) {
                logger.debug("Changing window height to " + n.intValue());
                s.setWindowHeight(n.intValue());
            }
        });
        w.maximizedProperty().addListener((c, o, n) -> {
            if (windowActive) {
                logger.debug("Changing window maximized to " + n);
                s.setMaximized(n);
            }
        });

        w.show();
        windowActive = true;

        // Fix bug with DPI scaling.
        // Window only calculates its right content size when resized AFTER being shown
        w.setWidth(w.getWidth() + 1);
    }

    public void setupBasicWindowContent() {
        layout = new GuiLayout();
        layout.setup();
        var title = "Pdx-Unlimiter (" + PdxuInstallation.getInstance().getVersion() + ")";
        var l = PdxuInstallation.getInstance().getLatestVersion();
        if (PdxuInstallation.getInstance().isProduction() &&
                l != null &&
                !l.equals(PdxuInstallation.getInstance().getVersion())) {
            title = title + "     OUTDATED: " + l + " available";
        }
        stage.setTitle(title);
        Scene scene = new Scene(layout.getContent());
        stage.setScene(scene);
        GuiStyle.addStylesheets(scene);
        layout.getContent().requestLayout();
    }

    public void setupCompleteWindowContent() {
        layout.finishSetup();
    }

    @Override
    public void start(Stage primaryStage) {
        ErrorHandler.setPlatformInitialized();

        APP = this;
        stage = primaryStage;

        try (var in = Files.newInputStream(PdxuInstallation.getInstance().getResourceDir().resolve("logo.png"))) {
            icon = new Image(in);
            primaryStage.getIcons().add(icon);
        } catch (IOException ex) {
            ErrorHandler.handleException(ex);
        }

        primaryStage.setOnCloseRequest(event -> {
            windowActive = false;

            ComponentManager.finalTeardown();
            // Close other windows
            Stage.getWindows().stream()
                    .filter(w -> !w.equals(stage))
                    .collect(Collectors.toList())
                    .forEach(w -> w.fireEvent(event));
        });

        ComponentManager.initialPlatformSetup();
    }

    public Image getIcon() {
        return icon;
    }
}
