package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import com.paradox_challenges.eu4_unlimiter.CommandLine;
import com.paradox_challenges.eu4_unlimiter.parser.GameDate;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4NormalParser;
import javafx.application.Application;
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

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SavegameManagerApp extends Application {

    private static Tooltip tooltip(String text) {

        Tooltip t = new Tooltip(text);
        t.setShowDelay(Duration.ZERO);
        return t;
    }

    private static HBox createRulerLabel(Eu4Campaign.Entry.Ruler ruler, boolean isRuler) {
        HBox box = new HBox();
        box.setSpacing(0);
        box.setAlignment(Pos.CENTER_LEFT);
        Tooltip.install(box, tooltip(ruler.getName()));
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        try {
            if (isRuler) {
                var i = Eu4ImageLoader.loadInterfaceImage("tab_domestic_court.dds");
                i.setViewport(new Rectangle2D(0, 5, 40, 44));
                box.getChildren().add(i);
            } else {
                box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("monarch_heir_crown_icon.dds"));
            }

            Label adm = new Label(" " + ruler.getAdm());
            adm.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
            box.getChildren().add(adm);
            box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_administrative_in_text.dds"));


            Label dip = new Label("/ " + ruler.getDip());
            dip.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
            box.getChildren().add(dip);
            box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_diplomatic_in_text.dds"));

            Label mil = new Label("/ " + ruler.getMil());
            mil.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
            box.getChildren().add(mil);
            box.getChildren().add(Eu4ImageLoader.loadInterfaceImage("icon_powers_military_in_text.dds"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        box.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        return box;
    }

    private GridPane createCampaignEntryNode(Eu4Campaign.Entry e) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setMaxHeight(120);
        grid.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");

        Label name = new Label(e.getDate().toString());
        name.setAlignment(Pos.CENTER);
        name.setPadding(new Insets(5, 5, 5, 5));
        name.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        grid.add(name, 0, 0, 3, 1);

        Label date = new Label( e.getDate().toString());
        date.setPrefHeight(200);
        date.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");

        grid.add(date, 0, 1);
        grid.add(createRulerLabel(e.getRuler(), true), 0, 2);
        if (e.getHeir().isPresent()) {
            grid.add(createRulerLabel(e.getHeir().get(), false), 0, 3);
        }


        Label version = new Label("v" + e.getVersion().toString());
        version.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");
        grid.add(version, 0, 4);


        Tooltip t = new Tooltip("Test");
        t.setShowDelay(Duration.ZERO);
        int wars = 0;
        for (Eu4Campaign.Entry.War war : e.getWars()) {
            if (wars >= 3) {
                break;
            }
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", war.getEnemies(), t), 1 + wars, 1);
            wars++;
        }
        grid.add(createDiplomacyRow("icon_alliance.dds", e.getAllies(), t), 1, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_royalmarriage.dds", e.getMarriages(), t), 2, 2);
        grid.add(createDiplomacyRow("icon_diplomacy_guaranting.dds", e.getGuarantees(), t), 3, 2);
        grid.add(createDiplomacyRow("icon_vassal.dds", e.getVassals(), t), 1, 3);
        grid.add(createDiplomacyRow("icon_vassal.dds", e.getJuniorPartners(), t), 2, 3);
        grid.add(createDiplomacyRow("subject_tributary_icon.dds", e.getTributaryJuniors(), t), 3, 3);
        grid.add(createDiplomacyRow("icon_march.dds", e.getMarches(), t), 1, 4);
        grid.add(createDiplomacyRow("icon_truce.dds", e.getTruces().keySet(), t), 2, 4);
        if (e.getSeniorPartner().isPresent()) {
            grid.add(createDiplomacyRow("icon_alliance.dds", Set.of(e.getSeniorPartner().get()), t), 4, 4);
        }
        return grid;
    }

    private Node createDiplomacyRow(String icon, Set<String> tags, Tooltip tooltip) {
        HBox box = new HBox();
        box.setSpacing(3);
        box.setAlignment(Pos.CENTER_LEFT);
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        try {
            box.getChildren().add(Eu4ImageLoader.loadInterfaceImage(icon));
            for (String tag : tags) {
                ImageView n = Eu4ImageLoader.loadFlagImage(tag, 20);
                box.getChildren().add(n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        box.setStyle("-fx-background-color: #777777; -fx-border-color: #666666; -fx-border-width: 3px;");
        box.setPadding(new Insets(0, 5, 0, 0));
        box.setMaxHeight(40);
        Tooltip.install(box, tooltip);
        return box;
    }

    private Node createCampaignNode() {
        //icon_truce, icon_alliance, icon_diplomacy_war, icon_vassal, icon_march, subject_tributary_icon, subject_tribute_icons, subject_colony_icon
        return null;
    }

    private void openCampaign(Eu4Campaign c) {
        mainGrid.add(createSavegameList(c), 1, 0);
    }

    private Button createCampaignButton(Eu4Campaign c) {
        ImageView w = Eu4ImageLoader.loadFlagImage(c.getSavegames().get(0).getCurrentTag(), 35);

        Pane countryColor = new Pane();
        countryColor.setStyle("-fx-background-color: black;");
        countryColor.setPrefSize(12,12);
        Button btn = new Button("", w);
        btn.setText("Name\nasd");
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                openCampaign(c);
            }
        });
        btn.setPadding(new Insets(5, 5, 5, 5));
        btn.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        btn.setBorder(Border.EMPTY);
        return btn;
    }

    private Node createSavegameList(Eu4Campaign c) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        for (Eu4Campaign.Entry e : c.getSavegames()) {
            GridPane button = createCampaignEntryNode(e);
            button.setMinWidth(400);
            grid.getChildren().add(button);
        }
        ScrollPane pane = new ScrollPane(grid);
        pane.setMinViewportWidth(200);
        return pane;
    }

    private Node createCampaignList(List<Eu4Campaign> data) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        for (Eu4Campaign d : data) {
            Button button = createCampaignButton(d);
            button.setMinWidth(200);
            grid.getChildren().add(button);
        }
        ScrollPane pane = new ScrollPane(grid);
        pane.setMinViewportWidth(200);
        return pane;
    }

    private GridPane mainGrid;

    @Override
    public void start(Stage primaryStage) {
        List<SavegameManager.SavegameData> data = new ArrayList<>(2);
        data.add(new SavegameManager.SavegameData(null, null, null, "Test"));
        for (int i = 0; i < 30; i++) {

            data.add(new SavegameManager.SavegameData(null, null, null, "Test2"));
        }

        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        mainGrid = new GridPane();

        Text scenetitle = new Text("Welcome");

        mainGrid.add(createCampaignList(SavegameCache.EU4_CACHE.getCampaigns()), 0, 0);

            Tooltip t = new Tooltip("Test");
            t.setShowDelay(Duration.ZERO);
            Node test = createDiplomacyRow("icon_diplomacy_war.dds", Set.of("ARA", "CAS"), t);
            mainGrid.add(createSavegameList(SavegameCache.EU4_CACHE.getCampaigns().get(0)), 1, 0);
            //mainGrid.add(createSavegameButton(SavegameCache.EU4_CACHE.getCampaigns().get(0).getSavegames().get(0)), 2, 0);

        primaryStage.setTitle("Hello World!");
        Scene scene = new Scene(mainGrid, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
        setUserAgentStylesheet(STYLESHEET_CASPIAN);
    }

    public static void main(String[] args) {
        try {
            CommandLine.main(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        launch(args);
    }
}
