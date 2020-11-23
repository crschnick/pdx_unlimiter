package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_ENTRY_LIST;

public class GuiGameCampaignEntryList {

    public static Node createCampaignEntryList() {
        JFXListView<Node> grid = new JFXListView<>();
        grid.getStyleClass().add(CLASS_ENTRY_LIST);

        SetChangeListener<GameCampaignEntry> l = (c) -> {
            Platform.runLater(() -> {
                if (c.wasAdded()) {
                    int index = new TreeSet<GameCampaignEntry>(c.getSet()).headSet(c.getElementAdded()).size();
                    grid.getItems().add(index, GuiGameCampaignEntry.createCampaignEntryNode(c.getElementAdded()));
                } else {
                    grid.getItems().remove(grid.getItems().stream()
                            .filter(n -> !c.getSet().contains(n.getProperties().get("entry"))).findAny().get());
                }
            });
        };

        GameIntegration.globalSelectedCampaignProperty().addListener((c, o, n) -> {
            if (o != null) {
                o.getSavegames().removeListener(l);
            }

            if (n != null) {
                n.getSavegames().addListener(l);
                Platform.runLater(() -> {
                    grid.setItems(FXCollections.observableArrayList(n.getSavegames().stream()
                            .map(GuiGameCampaignEntry::createCampaignEntryNode)
                            .collect(Collectors.toList())));
                });
            } else {
                Platform.runLater(() -> {
                    grid.setItems(FXCollections.observableArrayList());
                });
            }
        });

        GameIntegration.globalSelectedEntryProperty().addListener((c, o, n) -> {
            if (n != null) {
                int index = new TreeSet<GameCampaignEntry>(
                        GameIntegration.globalSelectedCampaignProperty().get().getSavegames()).headSet(n).size();
                grid.getSelectionModel().select(index);
                grid.getFocusModel().focus(index);
            } else {
            }
        });

        return grid;
    }
}
