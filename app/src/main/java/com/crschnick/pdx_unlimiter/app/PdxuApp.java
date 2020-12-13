package com.crschnick.pdx_unlimiter.app;

import com.crschnick.pdx_unlimiter.app.gui.GuiLayout;
import com.crschnick.pdx_unlimiter.app.gui.GuiStyle;
import com.crschnick.pdx_unlimiter.app.installation.*;
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

import java.util.stream.Collectors;

public class PdxuApp extends Application {

    private static PdxuApp APP;
    private Image icon;
    private StackPane layout;

    public static PdxuApp getApp() {
        return APP;
    }

    public static void main(String[] args) {
        ComponentManager.initialSetup(args);
        launch(args);
    }

    public Scene getScene() {
        return layout.getScene();
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
                    Stage.getWindows().stream()
                            .filter(w -> !w.equals(getScene().getWindow()))
                            .collect(Collectors.toList())
                            .forEach(w -> w.fireEvent(event));
                    ComponentManager.finalTeardown();
                }
            });

            layout = GuiLayout.createLayout();

            primaryStage.setTitle("Pdx-Unlimiter (" + PdxuInstallation.getInstance().getVersion() + ")");
            Scene scene = new Scene(layout, 1000, 720);
            primaryStage.setScene(scene);
            primaryStage.show();
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
