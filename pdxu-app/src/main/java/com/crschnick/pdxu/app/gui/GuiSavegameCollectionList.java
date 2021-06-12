package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.model.SavegameInfo;
import com.jfoenix.controls.JFXTextField;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;
import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_CAMPAIGN_TOP_BAR;

public class GuiSavegameCollectionList {


    public static <T, I extends SavegameInfo<T>> Node createCampaignList() {
        Region list = GuiListView.createViewOfList(
                SavegameManagerState.<T, I>get().getShownCollections(),
                GuiSavegameCollection::createCampaignButton);
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

        TextField filter = new JFXTextField();
        filter.setOnMouseClicked(e -> {
            filter.clear();
        });
        filter.textProperty().bindBidirectional(SavegameManagerState.get().getFilter().filterProperty());
        box.getChildren().add(filter);
        return box;
    }
}
