package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.Eu4SavegameImporter;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;

public class GuiGameCampaignList {


    public static Node createCampaignList() {
        JFXListView<Node> grid = new JFXListView<Node>();
        grid.getStyleClass().add(CLASS_CAMPAIGN_LIST);

        SetChangeListener<GameCampaign> l = (c) -> {
            Platform.runLater(() -> {
                if (c.wasAdded()) {
                    int index = new TreeSet<GameCampaign>(c.getSet()).headSet(c.getElementAdded()).size();
                    grid.getItems().add(index, GuiGameCampaign.createCampaignButton(c.getElementAdded()));
                    grid.getSelectionModel().select(index);
                    grid.getFocusModel().focus(index);
                } else {
                    grid.getItems().remove(grid.getItems().stream()
                            .filter(n -> !c.getSet().contains(n.getProperties().get("campaign"))).findAny().get());
                }
            });
        };

        GameIntegration.currentGameProperty().addListener((c, o, n) -> {
            if (n == null) {
                o.getSavegameCache().getCampaigns().removeListener(l);
                grid.setItems(FXCollections.observableArrayList());
            } else {
                n.getSavegameCache().getCampaigns().addListener(l);
                grid.setItems(FXCollections.observableArrayList(n.getSavegameCache().getCampaigns().stream()
                        .map(GuiGameCampaign::createCampaignButton)
                        .collect(Collectors.toList())));
            }
        });

        return grid;
    }

    public static Node createNoCampaignNode() {
        Label text = new Label("Welcome to the Pdx-Unlimiter!" +
                " To get started, import your latest EU4 savegame.");
        StackPane textPane = new StackPane(text);
        StackPane.setAlignment(textPane, Pos.CENTER);

        Button b = new Button("Import latest EU4 savegame");
        b.setOnMouseClicked(e -> {
            Eu4SavegameImporter.importLatestSavegame();
        });
        StackPane p = new StackPane();
        p.getChildren().add(b);
        StackPane.setAlignment(b, Pos.CENTER);
        VBox v = new VBox(textPane, new Label(), p);
        v.setAlignment(Pos.CENTER);
        return v;
    }
}
