package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.Main;
import com.crschnick.pdx_unlimiter.app.installation.Eu4App;
import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.Installation;
import com.crschnick.pdx_unlimiter.app.installation.PdxApp;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SavegameManagerApp extends Application {

    private void setCampaign(Optional<Eu4Campaign> c) {
        if (!this.selectedCampaign.equals(c)) {
            this.selectedCampaign = c;
        }

        savegameList.getChildren().clear();

        if (!c.isPresent()) {
            return;
        }

        if (c.get().getSavegames().size() > 0) {
            for (Eu4Campaign.Entry e : c.get().getSavegames()) {
                addEntry(e);
            }
        } else {
            SavegameCache.EU4_CACHE.loadAsync(c.get());
        }
    }

    private void onCampaignRemoved(Eu4Campaign c) {

    }

    private void onEntryRemoved(Eu4Campaign.Entry e) {
        for (Node n : this.savegameList.getChildren()) {
            if (n.getProperties().get("entry").equals(e)) {
                savegameList.getChildren().remove(n);
                return;
            }
        }
    }

    private void addEntry(Eu4Campaign.Entry e) {
        GridPane button = Eu4SavegameManagerStyle.createCampaignEntryNode(e, this.savegameList, (s) -> {
            this.selectedSave = Optional.of(s);
            this.saveName.setValue(s.getCurrentTag());
        }, (s) -> {
            SavegameCache.EU4_CACHE.delete(selectedCampaign.get(), e);
        });
        savegameList.getChildren().add(button);
    }

    private void createStatusThread(BorderPane layout) {
        Thread t = new Thread(() -> {
            Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createInactiveStatusBar(saveName)));

            Optional<PdxApp> oldApp = Optional.empty();
            while (true) {

                if (!oldApp.equals(PdxApp.getActiveApp())) {
                    var app = PdxApp.getActiveApp();
                    if (app.isPresent()) {
                        Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createActiveStatusBar(app.get())));
                    } else {
                        Platform.runLater(() -> layout.setBottom(Eu4SavegameManagerStyle.createInactiveStatusBar(saveName)));
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

    private VBox savegameList;

    private VBox campaignList;

    private Optional<Eu4Campaign> selectedCampaign = Optional.empty();

    private Optional<Eu4Campaign.Entry> selectedSave = Optional.empty();

    private StringProperty saveName = new SimpleStringProperty("");

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(Eu4ImageLoader.loadImage(Installation.EU4.get().getPath().resolve("launcher-assets").resolve("icon.png")));

        layout = new BorderPane();

        createStatusThread(layout);

        campaignList = Eu4SavegameManagerStyle.createCampaignList(SavegameCache.EU4_CACHE.getCampaigns(), (c) -> setCampaign(Optional.of(c)));
        layout.setLeft(Eu4SavegameManagerStyle.createCampaignScrollPane(campaignList));

        savegameList = Eu4SavegameManagerStyle.createSavegameList();
        layout.setCenter(Eu4SavegameManagerStyle.createSavegameScrollPane(savegameList));



        primaryStage.setTitle("Pdx Unlimiter");
        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
