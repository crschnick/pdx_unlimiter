package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.net.URI;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiGameCampaignEntryList {

    private static void addNoCampaignNodeListeners(Pane pane, Node listNode) {
        Consumer<Set<? extends GameCampaign<?, ?>>> update = (s) -> {
            Platform.runLater(() -> {
                if (s.size() > 0) {
                    pane.getChildren().set(0, listNode);
                } else {
                    pane.getChildren().set(0, createNoCampaignNode(pane));
                }
            });
        };

        SetChangeListener<GameCampaign<?, ?>> campaignListListener = (change) -> {
            update.accept(change.getSet());
        };

        GameIntegration.currentGameProperty().addListener((c, o, n) -> {
            if (o != null) {
                o.getSavegameCache().getCampaigns().removeListener(campaignListListener);
            }

            if (n != null) {
                n.getSavegameCache().getCampaigns().addListener(campaignListListener);
                update.accept(n.getSavegameCache().getCampaigns());
            }
        });
    }

    public static void createCampaignEntryList(Pane pane) {
        JFXListView<Node> grid = new JFXListView<>();
        grid.setOpacity(0.9);
        grid.getStyleClass().add(GuiStyle.CLASS_ENTRY_LIST);
        grid.prefWidthProperty().bind(pane.widthProperty());
        grid.prefHeightProperty().bind(pane.heightProperty());

        SetChangeListener<GameCampaignEntry<?, ?>> l = (c) -> {
            if (c.wasAdded()) {
                int index = GameIntegration.globalSelectedCampaignProperty().get().indexOf(
                        (GameCampaignEntry<Object, SavegameInfo<Object>>) c.getElementAdded());
                Platform.runLater(() -> {
                    grid.getItems().add(index, GuiGameCampaignEntry.createCampaignEntryNode(c.getElementAdded()));
                });
            } else {
                Platform.runLater(() -> {
                    grid.getItems().remove(grid.getItems().stream()
                            .filter(n -> !c.getSet().contains(n.getProperties().get("entry"))).findAny().get());
                });
            }
        };

        GameIntegration.globalSelectedCampaignProperty().addListener((c, o, n) -> {
            if (o != null) {
                o.getEntries().removeListener(l);
            }

            if (n != null) {
                Platform.runLater(() -> {
                    n.getEntries().addListener(l);
                    grid.setItems(FXCollections.observableArrayList(n.entryStream()
                            .map(GuiGameCampaignEntry::createCampaignEntryNode)
                            .collect(Collectors.toList())));

                    // Bug in JFoenix? We have to set this everytime we update the list view
                    grid.setExpanded(true);
                });
            } else {
                Platform.runLater(() -> {
                    grid.setItems(FXCollections.observableArrayList());
                });

            }
        });

        GameIntegration.globalSelectedEntryProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                grid.getSelectionModel().clearSelection();
                if (n != null) {
                    int index = GameIntegration.globalSelectedCampaignProperty().get().indexOf(n);
                    grid.getSelectionModel().clearSelection();
                    grid.getSelectionModel().select(index);
                }
            });
        });

        addNoCampaignNodeListeners(pane, grid);
    }

    private static Node createNoCampaignNode(Pane pane) {
        VBox v = new VBox();
        Label text = new Label("It seems like there are no imported savegames for " +
                GameIntegration.current().getName() + " yet.\n");
        v.getChildren().add(text);

        Button importB = new Button("Import savegames");
        importB.setOnAction(e -> {
            GuiImporter.createImporterDialog(GameIntegration.current().getSavegameWatcher());
            e.consume();
        });
        importB.setGraphic(new FontIcon());
        importB.getStyleClass().add(GuiStyle.CLASS_IMPORT);
        v.getChildren().add(importB);

        v.getChildren().add(new Label());
        Label text2 = new Label("If you want to familiarize yourself with the Pdx-Unlimiter first, " +
                "it is recommended to read the guide.");
        text2.setWrapText(true);
        text2.setTextAlignment(TextAlignment.CENTER);
        v.getChildren().add(text2);

        Button guide = new Button("Read the guide");
        guide.setOnAction((a) -> {
            try {
                Desktop.getDesktop().browse(
                        new URI("https://github.com/crschnick/pdx_unlimiter/blob/master/docs/GUIDE.md"));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
        v.getChildren().add(guide);

        v.getStyleClass().add(GuiStyle.CLASS_NO_CAMPAIGN);
        v.setFillWidth(true);
        v.setSpacing(10);
        v.setAlignment(Pos.CENTER);
        v.prefWidthProperty().bind(pane.widthProperty());
        v.prefHeightProperty().bind(pane.heightProperty());
        return v;
    }
}
