package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Border;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;

public class GuiGameCampaignList {


    public static Node createCampaignList() {
        JFXListView<Node> grid = new JFXListView<Node>();
        grid.setBorder(Border.EMPTY);
        grid.getStyleClass().add(CLASS_CAMPAIGN_LIST);

        Consumer<GameCampaign<?, ?>> addButton = (GameCampaign<?, ?> c) -> {
            var button = GuiGameCampaign.createCampaignButton(c);
            int index = GameIntegration.current().getSavegameCache().indexOf(c);
            grid.getItems().add(index, button);
        };

        SetChangeListener<GameCampaign> l = (c) -> {
            Platform.runLater(() -> {
                if (c.wasAdded()) {
                    addButton.accept(c.getElementAdded());
                } else {
                    grid.getItems().remove(grid.getItems().stream()
                            .filter(n -> !c.getSet().contains(n.getProperties().get("campaign"))).findAny().get());
                }
            });
        };

        GameIntegration.currentGameProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                if (o != null) {
                    o.getSavegameCache().getCampaigns().removeListener(l);
                }

                if (n == null) {
                    grid.setItems(FXCollections.observableArrayList());
                } else {
                    grid.setItems(FXCollections.observableArrayList(n.getSavegameCache().campaignStream()
                            .map(GuiGameCampaign::createCampaignButton)
                            .collect(Collectors.toList())));
                    n.getSavegameCache().getCampaigns().addListener(l);
                }
            });
        });

        GameIntegration.globalSelectedCampaignProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                grid.getSelectionModel().clearSelection();
                if (n != null) {
                    int index = GameIntegration.current().getSavegameCache().indexOf(n);
                    grid.getSelectionModel().select(index);
                }
            });
        });
        return grid;
    }
}
