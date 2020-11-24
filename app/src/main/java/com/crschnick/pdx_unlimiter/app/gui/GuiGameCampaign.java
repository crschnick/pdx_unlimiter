package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.game.GameCampaign;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiGameCampaign {


    static HBox createCampaignButton(GameCampaign c) {
        Button del = new JFXButton();
        del.setGraphic(new FontIcon());
        del.getStyleClass().add("delete-button");
        del.setOnMouseClicked((m) -> {
            if (DialogHelper.showCampaignDeleteDialog()) {
                GameIntegration.current().getSavegameCache().delete(c);
            }
        });
        del.setAlignment(Pos.CENTER);


        JFXTextField name = new JFXTextField(c.getName());
        name.getStyleClass().add(CLASS_TEXT_FIELD);
        name.textProperty().bindBidirectional(c.nameProperty());

        Label date = new Label();
        date.textProperty().bind(GameIntegration.current().getGuiFactory().createInfoString(c));
        date.getStyleClass().add(CLASS_DATE);

        HBox top = new HBox();
        top.getChildren().add(name);
        top.getChildren().add(del);
        top.setAlignment(Pos.CENTER);

        VBox b = new VBox();
        b.getChildren().add(top);
        b.getChildren().add(date);
        b.setAlignment(Pos.CENTER_LEFT);

        ObservableValue<Pane> prop = GameIntegration.current().getGuiFactory().createImage(c);
        Node w = prop.getValue();
        HBox btn = new HBox();
        btn.getChildren().add(w);
        btn.getChildren().add(b);
        prop.addListener((change, o, n) -> {
            Platform.runLater(() -> {
                btn.getChildren().set(0, prop.getValue());
            });
        });

        btn.setOnMouseClicked((m) -> GameIntegration.current().selectCampaign(c));
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add(CLASS_CAMPAIGN_LIST_ENTRY);
        btn.getProperties().put("campaign", c);
        return btn;
    }


}
