package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.core.settings.SavedState;
import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(PdxuApp.class);

    private static PdxuApp APP;
    private Image icon;
    private StackPane layout;
    private boolean windowActive;

    public static PdxuApp getApp() {
        return APP;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Scene getScene() {
        return layout.getScene();
    }

    public void setupWindowState() {
        Platform.runLater(() -> {
            Scene scene = getScene();
            Stage w = (Stage) getScene().getWindow();
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

            scene.getWindow().xProperty().addListener((c, o, n) -> {
                if (windowActive) {
                    logger.debug("Changing window x to " + n.intValue());
                    s.setWindowX(n.intValue());
                }
            });
            scene.getWindow().yProperty().addListener((c, o, n) -> {
                if (windowActive) {
                    logger.debug("Changing window y to " + n.intValue());
                    s.setWindowY(n.intValue());
                }
            });
            scene.getWindow().widthProperty().addListener((c, o, n) -> {
                if (windowActive) {
                    logger.debug("Changing window width to " + n.intValue());
                    s.setWindowWidth(n.intValue());
                }
            });
            scene.getWindow().heightProperty().addListener((c, o, n) -> {
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
        });
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            APP = this;
            icon = new Image(PdxuApp.class.getResourceAsStream("logo.png"));
            primaryStage.getIcons().add(icon);
            primaryStage.setOnCloseRequest(event -> {
                windowActive = false;

                ComponentManager.finalTeardown();
                // Close other windows
                Stage.getWindows().stream()
                        .filter(w -> !w.equals(getScene().getWindow()))
                        .collect(Collectors.toList())
                        .forEach(w -> w.fireEvent(event));
            });

            layout = GuiLayout.createLayout();

            primaryStage.setTitle("Pdx-Unlimiter (" + PdxuInstallation.getInstance().getVersion() + ")");
            Scene scene = new Scene(layout, 1000, 720);
            primaryStage.setScene(scene);
            GuiStyle.addStylesheets(primaryStage.getScene());
            ComponentManager.additionalSetup();
        } catch (Exception ex) {
            ErrorHandler.handleTerminalException(ex);
        }

    }

    public Image getIcon() {
        return icon;
    }
}
