package com.paradox_challenges.eu4_unlimiter.savegame_mgr;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SavegameManagerApp extends Application {

    private Button createSavegameButton(SavegameManager.SavegameData data) {
        ImageView w = null;

        try {


            w = Eu4ImageLoader.loadFlagImage("CAS");


        } catch (IOException ex) {
            ex.printStackTrace();


        }
        Pane countryColor = new Pane();
        countryColor.setStyle("-fx-background-color: black;");
        countryColor.setPrefSize(12,12);
        Button btn = new Button(data.getName(), w);
        btn.setText("<b>Name\nasd");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        btn.setPadding(new Insets(5, 5, 5, 5));
        btn.setStyle("-fx-border-color: #666666; -fx-border-width: 3px; -fx-background-color: #777777;-fx-text-fill: white; -fx-font-size: 15px;");
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

        primaryStage.setTitle("Hello World!");
        Scene scene = new Scene(mainGrid, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
