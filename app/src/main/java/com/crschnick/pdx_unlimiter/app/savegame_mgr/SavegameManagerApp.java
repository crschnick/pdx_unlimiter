package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.Main;
import com.crschnick.pdx_unlimiter.app.installation.Eu4App;
import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SavegameManagerApp extends Application {

    private void createStatusThread(BorderPane layout) {
        Thread t = new Thread(() -> {
            Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createInactiveStatusBar(selectedSave)));

            Optional<PdxApp> oldApp = Optional.empty();
            while (true) {

                if (!oldApp.equals(PdxApp.getActiveApp())) {
                    var app = PdxApp.getActiveApp();
                    if (app.isPresent()) {
                        Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createActiveStatusBar(app.get())));
                    } else {
                        Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createInactiveStatusBar(selectedSave)));
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

    @Override
    public void start(Stage primaryStage) {
        layout = new BorderPane();

        createStatusThread(layout);

        layout.setLeft(Eu4SavegameManagerStyle.createCampaignList(SavegameCache.EU4_CACHE.getCampaigns(), selectedCampaign, (c) -> SavegameCache.EU4_CACHE.delete(c)));
        layout.setCenter(Eu4SavegameManagerStyle.createSavegameScrollPane(selectedCampaign, selectedSave, (e) -> SavegameCache.EU4_CACHE.delete(selectedCampaign.get().get(), e)));

        selectedCampaign.addListener((c,o,n) -> {
            if (n.isPresent()) {
                SavegameCache.EU4_CACHE.loadAsync(n.get());
            }
        });

        primaryStage.setTitle("Pdx Unlimiter");
        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        setUserAgentStylesheet(STYLESHEET_CASPIAN);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                running.setValue(false);
            }
        });

        //Eu4SavegameImporter.importAllSavegames(running);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
