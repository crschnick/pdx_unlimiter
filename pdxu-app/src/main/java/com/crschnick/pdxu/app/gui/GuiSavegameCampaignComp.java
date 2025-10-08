package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.comp.base.IconButtonComp;
import com.crschnick.pdxu.app.core.AppFontSizes;
import com.crschnick.pdxu.app.core.window.AppDialog;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameCampaign;
import com.crschnick.pdxu.app.savegame.SavegameContext;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;

import static com.crschnick.pdxu.app.gui.GuiStyle.*;

@AllArgsConstructor
public class GuiSavegameCampaignComp<T, I extends SavegameInfo<T>> extends SimpleComp {

    private final SavegameCampaign<T, I> campaign;
    private final Runnable onSelect;

    @Override
    protected Region createSimple() {
        HBox btn = new HBox();
        btn.setOnMouseClicked((m) -> {
            onSelect.run();
        });
        btn.setAlignment(Pos.CENTER);
        btn.getStyleClass().add(CLASS_CAMPAIGN_LIST_ENTRY);

        {
            SavegameContext.withCollectionContext(campaign, gi -> {
                ObservableValue<Node> prop = gi.getGuiFactory().createImage(campaign);
                prop.addListener((change, o, n) -> {
                    Platform.runLater(() -> {
                        btn.getChildren().set(0, prop.getValue());
                    });
                });
                Node w = prop.getValue();
                w.getStyleClass().add(CLASS_TAG_ICON);
                btn.getChildren().add(w);
                btn.getStyleClass().add(gi.getGame().getId() + "-campaign");
            });
        }

        VBox info = new VBox();
        info.setFillWidth(true);
        info.setAlignment(Pos.CENTER_LEFT);
        {
            HBox top = new HBox();
            top.setPadding(new Insets(0, 2, 0, 0));
            top.setAlignment(Pos.CENTER);

            var name = new TextField(campaign.getName());
            name.getStyleClass().add(CLASS_TEXT_FIELD);
            name.setAccessibleText("Campaign name");
            name.textProperty().bindBidirectional(campaign.nameProperty());
            top.getChildren().add(name);
            AppFontSizes.xxl(name);

            var del = new IconButtonComp(new LabelGraphic.IconGraphic("mdi2t-trash-can-outline"), () -> {
                var confirm = AppDialog.confirm("deleteCampaign");
                if (confirm) {
                    SavegameActions.delete(campaign);
                }
            }).createRegion();
            del.setAccessibleText("Delete campaign");
            top.getChildren().add(del);

            info.getChildren().add(top);
        }

        {
            HBox bottom = new HBox();

            Label date = new Label();
            SavegameContext.withCollectionContext(campaign, gi -> {
                date.textProperty().bind(gi.getGuiFactory().createInfoString(campaign));
            });
            date.getStyleClass().add(CLASS_DATE);
            date.setAccessibleText("Date");
            bottom.getChildren().add(date);
            AppFontSizes.lg(date);

            Region spacer = new Region();
            bottom.getChildren().add(spacer);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label count = new Label("[" + campaign.getSavegames().size() + "]");
            count.getStyleClass().add(CLASS_DATE);
            count.setAlignment(Pos.CENTER_LEFT);
            campaign.getSavegames().addListener((SetChangeListener<SavegameEntry<?, ?>>) change -> {
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
        btn.accessibleTextProperty().bind(campaign.nameProperty());
        btn.setPrefWidth(250);
        return btn;
    }
}
