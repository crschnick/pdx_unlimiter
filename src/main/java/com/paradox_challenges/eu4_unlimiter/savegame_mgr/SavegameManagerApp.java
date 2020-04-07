package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

import com.paradox_challenges.eu4_unlimiter.CommandLine;
import com.paradox_challenges.eu4_unlimiter.parser.GameDate;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4NormalParser;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SavegameManagerApp extends Application {

    private Node createSavegameNode(SavegameManager.SavegameData data) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setMaxHeight(120);
        grid.setStyle("-fx-background-color: #555555; -fx-border-color: #666666; -fx-border-width: 3px;");

        Label name = new Label(data.getName()+ " sadsad sad sad sad sa d");
        name.setAlignment(Pos.CENTER);
        name.setPadding(new Insets(5, 5, 5, 5));
        name.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        grid.add(name, 0, 0, 3, 1);

        Label date = new Label("Date: " + data.getDate());
        date.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");

        Label ruler = new Label("Ruler: ");
        ruler.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");


        Label heir = new Label("Heir: ");
        heir.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        grid.add(date, 0, 1);
        grid.add(ruler, 0, 2);
        grid.add(heir, 0, 3);
        try {
            Tooltip t = new Tooltip("Test");
            t.setShowDelay(Duration.ZERO);
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", List.of("ARA", "CAS", "VEN"), t), 1, 1);
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", List.of("ARA", "CAS"), t), 2, 1);
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", List.of("ARA", "CAS"), t), 1, 2);
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", List.of("ARA", "CAS"), t), 1, 3);
            grid.add(createDiplomacyRow("icon_diplomacy_war.dds", List.of("ARA", "CAS"), t), 2, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return grid;
    }

    private Node createDiplomacyRow(String icon, List<String> tags, Tooltip tooltip) throws IOException {
        HBox box = new HBox();
        box.setSpacing(3);
        box.setAlignment(Pos.CENTER_LEFT);
        //flag_smallest_overlay.dds, shield_fancy_mask.tga
        box.getChildren().add(Eu4ImageLoader.loadInterfaceImage(icon));
        for (String tag : tags) {
            ImageView n = Eu4ImageLoader.loadFlagImage(tag, 20);
            box.getChildren().add(n);
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

    private Button createSavegameButton(SavegameManager.SavegameData data) {
        ImageView w = null;

        try {


            w = Eu4ImageLoader.loadFlagImage("ARA", 35);


        } catch (IOException ex) {
            ex.printStackTrace();


        }
        Pane countryColor = new Pane();
        countryColor.setStyle("-fx-background-color: black;");
        countryColor.setPrefSize(12,12);
        Button btn = new Button(data.getName(), w);
        btn.setText("Name\nasd");
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        btn.setPadding(new Insets(5, 5, 5, 5));
        btn.setStyle("-fx-border-color: #666666; -fx-background-radius: 0; -fx-border-radius: 0; -fx-border-insets: 3 0 3 0; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
        btn.setBorder(Border.EMPTY);
        return btn;
    }

    private Node createSavegameList(List<SavegameManager.SavegameData> data) {
        VBox grid = new VBox();
        grid.setFillWidth(true);
        grid.setPadding(new Insets(0, 0, 0, 0));
        grid.setSpacing(3);

        int index = 0;
        for (SavegameManager.SavegameData d : data) {
            Button button = createSavegameButton(d);
            button.setMinWidth(200);
            grid.getChildren().add(button);
            index++;
        }
        ScrollPane pane = new ScrollPane(grid);
        pane.setMinViewportWidth(200);
        return pane;
    }

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

        GridPane mainGrid = new GridPane();

        Text scenetitle = new Text("Welcome");

        mainGrid.add(createSavegameList(data), 0, 0);

        try {
            Tooltip t = new Tooltip("Test");
            t.setShowDelay(Duration.ZERO);
            Node test = createDiplomacyRow("icon_diplomacy_war", List.of("ARA", "CAS"), t);
            mainGrid.add(createSavegameNode(new SavegameManager.SavegameData("ABC", "ABC", new GameDate(1, Month.APRIL, 1444), "Test name")), 1, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryStage.setTitle("Hello World!");
        Scene scene = new Scene(mainGrid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
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
