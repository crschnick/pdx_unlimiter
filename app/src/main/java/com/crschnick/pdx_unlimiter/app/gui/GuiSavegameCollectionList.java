package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_CAMPAIGN_TOP_BAR;

public class GuiSavegameCollectionList {


    public static <T, I extends SavegameInfo<T>> Node createCampaignList() {
        ListView<Node> list = GuiListView.createViewOfList(
                SavegameManagerState.<T, I>get().getShownCollections(),
                GuiSavegameCollection::createCampaignButton,
                SavegameManagerState.<T, I>get().globalSelectedCampaignProperty());
        list.getStyleClass().add(CLASS_CAMPAIGN_LIST);

        var top = createTopBar();
        var box = new VBox(top, list);
        top.prefWidthProperty().bind(box.widthProperty());
        VBox.setVgrow(list, Priority.ALWAYS);
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
        return box;
    }
}
