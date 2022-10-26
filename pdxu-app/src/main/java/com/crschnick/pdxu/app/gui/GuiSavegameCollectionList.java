package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;
import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_CAMPAIGN_TOP_BAR;

public class GuiSavegameCollectionList {


    public static <T, I extends SavegameInfo<T>> Node createCampaignList() {
        Region list = GuiListView.createViewOfList(
                SavegameManagerState.<T, I>get().getShownCollections(),
                GuiSavegameCampaign::createCampaignButton);
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

        var searchIcon = new StackPane();
        searchIcon.getStyleClass().add(GuiStyle.CLASS_SEARCH);
        searchIcon.getChildren().add(new FontIcon());
        box.getChildren().add(searchIcon);

        TextField filter = new JFXTextField();
        filter.setOnMouseClicked(e -> {
            filter.clear();
        });
        filter.textProperty().bindBidirectional(SavegameManagerState.get().getFilter().filterProperty());
        box.getChildren().add(filter);
        HBox.setHgrow(filter, Priority.ALWAYS);
        return box;
    }
}
