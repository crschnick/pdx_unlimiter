package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.Border;

import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;

public class GuiGameCampaignList {


    public static Node createCampaignList() {
        JFXListView<Node> grid = new JFXListView<Node>();
        grid.setBorder(Border.EMPTY);
        grid.getStyleClass().add(CLASS_CAMPAIGN_LIST);

        SetChangeListener<GameCampaign<?, ?>> l = (c) -> {
            if (c.wasAdded()) {
                var button = GuiGameCampaign.createCampaignButton(
                        c.getElementAdded(), GameIntegration.current().getGuiFactory());
                int index = GameIntegration.current().getSavegameCache().indexOf(c.getElementAdded());
                Platform.runLater(() -> {
                    grid.getItems().add(index, button);
                });
            } else {
                Platform.runLater(() -> grid.getItems().remove(grid.getItems().stream()
                        .filter(n -> !c.getSet().contains(n.getProperties().get("campaign"))).findAny().get()));
            }
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
                            .map(camp -> GuiGameCampaign.createCampaignButton(camp, n.getGuiFactory()))
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
