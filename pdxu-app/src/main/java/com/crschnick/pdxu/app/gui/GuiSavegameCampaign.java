package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.lang.PdxuI18n;
import com.crschnick.pdxu.app.savegame.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

public class GuiSavegameCampaign {

    public static <T, I extends SavegameInfo<T>> Node createCampaignButton(
            SavegameCampaign<T, I> c) {
        HBox btn = new HBox();
        btn.setOnMouseClicked((m) -> SavegameManagerState.<T, I>get().selectCollectionAsync(c));
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add(CLASS_CAMPAIGN_LIST_ENTRY);

        {
            SavegameContext.withCollectionContext(c, gi -> {
                ObservableValue<Node> prop = gi.getGuiFactory().createImage(c);
                prop.addListener((change, o, n) -> {
                    Platform.runLater(() -> {
                        btn.getChildren().set(0, prop.getValue());
                    });
                });
                Node w = prop.getValue();
                w.getStyleClass().add(CLASS_TAG_ICON);
                btn.getChildren().add(w);
            });
        }

        VBox info = new VBox();
        info.setFillWidth(true);
        info.setAlignment(Pos.CENTER_LEFT);
        {
            HBox top = new HBox();
            top.setAlignment(Pos.CENTER);

            JFXTextField name = new JFXTextField(c.getName());
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.setAccessibleText("Campaign name");
            name.textProperty().bindBidirectional(c.nameProperty());
            top.getChildren().add(name);

            Button del = new JFXButton(null, new FontIcon());
            del.getStyleClass().add("delete-button");
            del.setAccessibleText("Delete campaign");
            del.setOnMouseClicked((m) -> {
                if (GuiDialogHelper.showBlockingAlert(alert -> {
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.setTitle(PdxuI18n.get("DELETE_CAMPAIGN_TITLE"));
                    alert.setHeaderText(PdxuI18n.get("DELETE_CAMPAIGN_QUESTION"));
                }).map(t -> t.getButtonData().isDefaultButton()).orElse(false)) {
                    SavegameActions.delete(c);
                }
            });
            del.setAlignment(Pos.CENTER);
            top.getChildren().add(del);

            info.getChildren().add(top);
        }

        {
            HBox bottom = new HBox();

            Label date = new Label();
            SavegameContext.withCollectionContext(c, gi -> {
                date.textProperty().bind(gi.getGuiFactory().createInfoString(c));
            });
            date.getStyleClass().add(CLASS_DATE);
            date.setAccessibleText("Date");
            bottom.getChildren().add(date);

            Region spacer = new Region();
            bottom.getChildren().add(spacer);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label count = new Label("[" + c.getSavegames().size() + "]");
            count.getStyleClass().add(CLASS_DATE);
            count.setAlignment(Pos.CENTER_LEFT);
            c.getSavegames().addListener((SetChangeListener<SavegameEntry<?, ?>>) change -> {
                int newLength = change.getSet().size();
                Platform.runLater(() -> {
                    count.setText("[" + newLength + "]");
                });
            });
            bottom.getChildren().add(count);

            info.getChildren().add(bottom);
        }
        btn.getChildren().add(info);
        btn.setCursor(Cursor.HAND);
        btn.accessibleTextProperty().bind(c.nameProperty());
        return btn;
    }
}
