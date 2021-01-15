package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.stream.Collectors;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_TOP_BAR;

public class GuiGameCampaignList {


    public static Node createCampaignList() {
        JFXListView<Node> grid = new JFXListView<Node>();
        grid.setBorder(Border.EMPTY);
        grid.getStyleClass().add(CLASS_CAMPAIGN_LIST);

        SetChangeListener<SavegameCollection<?, ?>> l = (c) -> {
            if (c.wasAdded()) {
                var button = GuiGameCampaign.createCampaignButton(
                        c.getElementAdded(), SavegameManagerState.get().current().getGuiFactory());
                int index = SavegameManagerState.get().indexOf(c.getElementAdded());
                Platform.runLater(() -> {
                    grid.getItems().add(index, button);
                });
            } else {
                Platform.runLater(() -> grid.getItems().remove(grid.getItems().stream()
                        .filter(n -> !c.getSet().contains(n.getProperties().get("campaign"))).findAny().get()));
            }
        };

        SavegameManagerState.get().shownCollectionsProperty().addListener(l);

        SavegameManagerState.get().globalSelectedCampaignProperty().addListener((c, o, n) -> {
            Platform.runLater(() -> {
                grid.getSelectionModel().clearSelection();
                if (n != null) {
                    int index = SavegameManagerState.get().current().getSavegameCache().indexOf(n);
                    grid.getSelectionModel().select(index);
                }
            });
        });

        var top = createTopBar();
        var box = new VBox(top, grid);
        top.prefWidthProperty().bind(box.widthProperty());
        VBox.setVgrow(grid, Priority.ALWAYS);
        return box;
    }

    private static Region createTopBar() {
        HBox box = new HBox();
        box.setSpacing(8);
        box.getStyleClass().add(CLASS_CAMPAIGN_TOP_BAR);
        box.setAlignment(Pos.CENTER);
        Button create = new Button();
        create.getStyleClass().add(GuiStyle.CLASS_NEW);
        create.setGraphic(new FontIcon());
        create.setOnAction(e -> {
            SavegameManagerState.get().current().getSavegameCache().addNewFolder("New Folder");
            e.consume();
        });
        GuiTooltips.install(create, "Create new folder");
        box.getChildren().add(create);

        box.getChildren().add(new Separator(Orientation.VERTICAL));

        TextField filter = new JFXTextField();
        filter.setOnMouseClicked(e -> {
            filter.clear();
        });
        filter.textProperty().bindBidirectional(SavegameManagerState.get().getFilter().filterProperty());
        box.getChildren().add(filter);

        ToggleButton deepSearch = new ToggleButton();
        deepSearch.getStyleClass().add(GuiStyle.CLASS_RECURSIVE);
        deepSearch.setGraphic(new FontIcon());
        deepSearch.selectedProperty().bindBidirectional(SavegameManagerState.get().getFilter().deepSearchProperty());
        GuiTooltips.install(deepSearch, "Include individual savegame names in search");
        box.getChildren().add(deepSearch);
        return box;
    }
}
