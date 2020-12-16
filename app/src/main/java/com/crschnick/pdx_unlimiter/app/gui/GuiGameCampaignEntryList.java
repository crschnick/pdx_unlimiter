package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
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

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiGameCampaignEntryList {

    private static void addNoCampaignNodeListeners(Pane pane, Node listNode) {
        Consumer<Set<? extends GameCampaign<?,?>>> update = (s) -> {
            Platform.runLater(() -> {
                if (s.size() > 0) {
                    pane.getChildren().set(0, listNode);
                } else {
                    pane.getChildren().set(0, createNoCampaignNode(pane));
                }
            });
        };

        SetChangeListener<GameCampaign<?,?>> campaignListListener = (change) -> {
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

        SetChangeListener<GameCampaignEntry> l = (c) -> {
            Platform.runLater(() -> {
                if (c.wasAdded()) {
                    int index = GameIntegration.globalSelectedCampaignProperty().get().indexOf(c.getElementAdded());
                    grid.getItems().add(index, GuiGameCampaignEntry.createCampaignEntryNode(c.getElementAdded()));
                } else {
                    grid.getItems().remove(grid.getItems().stream()
                            .filter(n -> !c.getSet().contains(n.getProperties().get("entry"))).findAny().get());
                }
            });
        };

        GameIntegration.globalSelectedCampaignProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                if (o != null) {
                    o.getEntries().removeListener(l);
                }

                if (n != null) {
                    n.getEntries().addListener(l);
                    grid.setItems(FXCollections.observableArrayList(n.entryStream()
                            .map(GuiGameCampaignEntry::createCampaignEntryNode)
                            .collect(Collectors.toList())));

                    // Bug in JFoenix? We have to set this everytime we update the list view
                    grid.setExpanded(true);
                } else {
                    grid.setItems(FXCollections.observableArrayList());

                }
            });
        });

        GameIntegration.globalSelectedEntryProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                grid.getSelectionModel().clearSelection();
                if (n != null) {
                    int index = GameIntegration.globalSelectedCampaignProperty().get().indexOf(n);
                    grid.getSelectionModel().clearSelection();
                    grid.scrollTo(index);
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
        Label text2 = new Label("Furthermore, you can use the status bar below to import the latest savegame at any time. " +
                "Alternatively, can also drag and drop any savegame into " +
                "this window or double click any savegame file if its extension is associated with the Pdx-Unlimiter.");
        text2.setWrapText(true);
        text2.setTextAlignment(TextAlignment.CENTER);
        v.getChildren().add(text2);

        v.getStyleClass().add(GuiStyle.CLASS_NO_CAMPAIGN);
        v.setFillWidth(true);
        v.setSpacing(10);
        v.setAlignment(Pos.CENTER);
        v.prefWidthProperty().bind(pane.widthProperty());
        v.prefHeightProperty().bind(pane.heightProperty());
        return v;
    }
}
