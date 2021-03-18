package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.ComponentManager;
import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiDialogHelper;
import com.crschnick.pdx_unlimiter.app.savegame.*;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Optional;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.*;

public class GuiSavegameCollection {

    public static <T, I extends SavegameInfo<T>> Node createCampaignButton(
            SavegameCollection<T, I> c) {
        HBox btn = new HBox();
        btn.setOnMouseClicked((m) -> ComponentManager.selectCollection(c));
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add(CLASS_CAMPAIGN_LIST_ENTRY);

        {
            if (c instanceof SavegameCampaign) {
                SavegameCampaign<T, I> ca = (SavegameCampaign<T, I>) c;
                SavegameContext.withCollection(c, gi -> {
                    ObservableValue<Node> prop = gi.getGuiFactory().createImage(ca);
                    prop.addListener((change, o, n) -> {
                        Platform.runLater(() -> {
                            btn.getChildren().set(0, prop.getValue());
                        });
                    });
                    Node w = prop.getValue();
                    btn.getChildren().add(w);
                });
            } else {
                Node l = new FontIcon();
                l.getStyleClass().add(CLASS_FOLDER);
                l.getStyleClass().add(CLASS_TAG_ICON);
                btn.getChildren().add(l);
            }
        }

        VBox info = new VBox();
        info.setFillWidth(true);
        info.setAlignment(Pos.CENTER_LEFT);
        {
            HBox top = new HBox();
            top.setAlignment(Pos.CENTER);

            JFXTextField name = new JFXTextField(c.getName());
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.textProperty().bindBidirectional(c.nameProperty());
            top.getChildren().add(name);

            Button del = new JFXButton();
            del.setGraphic(new FontIcon());
            del.getStyleClass().add("delete-button");
            del.setOnMouseClicked((m) -> {
                if (GuiDialogHelper.showBlockingAlert(alert -> {
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm deletion");
                    alert.setHeaderText("Do you want to delete the selected campaign? This will delete all savegames of it.");
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

            if (c instanceof SavegameCampaign) {
                SavegameCampaign<T, I> ca = (SavegameCampaign<T, I>) c;
                Label date = new Label();
                SavegameContext.withCollection(c, gi -> {
                    date.textProperty().bind(gi.getGuiFactory().createInfoString(ca));
                });
                date.getStyleClass().add(CLASS_DATE);
                bottom.getChildren().add(date);
            }

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

        setupDragAndDrop(c, btn);
        return btn;
    }

    private static <T, I extends SavegameInfo<T>> void setupDragAndDrop(SavegameCollection<T, I> c, HBox btn) {
        btn.setOnDragOver(event -> {
            if (event.getGestureSource() != btn && event.getSource() instanceof Node) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                btn.getChildren().get(0).setEffect(new Glow(1));
            }

            event.consume();
        });

        btn.setOnDragExited(event -> {
            btn.getChildren().get(0).setEffect(null);
        });

        btn.setOnDragDropped(de -> {
            Node src = (Node) de.getGestureSource();
            @SuppressWarnings("unchecked")
            SavegameEntry<T, I> entry = (SavegameEntry<T, I>) src.getProperties().get("list-item");
            SavegameActions.moveEntry(c, entry);
        });
    }

}
