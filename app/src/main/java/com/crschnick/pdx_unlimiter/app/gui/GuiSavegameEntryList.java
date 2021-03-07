package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCollection;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Set;
import java.util.function.Consumer;

public class GuiSavegameEntryList {

    private static void addNoCampaignNodeListeners(Pane pane, Node listNode) {
        Consumer<Set<? extends SavegameCollection<?, ?>>> update = (s) -> {
            int newSize = s.size();
            Platform.runLater(() -> {
                if (newSize > 0) {
                    pane.getChildren().set(0, listNode);
                } else {
                    pane.getChildren().set(0, createNoCampaignNode(pane));
                }
            });
        };

        SetChangeListener<SavegameCollection<?, ?>> campaignListListener = (change) -> {
            update.accept(change.getSet());
        };

        //TODO: change
        SavegameManagerState.get().currentGameProperty().addListener((c, o, n) -> {
            if (o != null) {
                var storage = SavegameStorage.get(o);
                storage.getCollections().removeListener(campaignListListener);
            }

            if (n != null) {
                var storage = SavegameStorage.get(n);
                storage.getCollections().addListener(campaignListListener);
                update.accept(storage.getCollections());
            }
        });
    }

    public static void createCampaignEntryList(Pane pane) {
        ListView<Node> grid = GuiListView.createViewOfList(
                SavegameManagerState.get().getShownEntries(),
                GuiSavegameEntry::createSavegameEntryNode,
                SavegameManagerState.get().globalSelectedEntryProperty());
        grid.setOpacity(0.9);
        grid.getStyleClass().add(GuiStyle.CLASS_ENTRY_LIST);
        grid.prefWidthProperty().bind(pane.widthProperty());
        grid.prefHeightProperty().bind(pane.heightProperty());

        addNoCampaignNodeListeners(pane, grid);
    }

    private static Node createNoCampaignNode(Pane pane) {
        VBox v = new VBox();
        Label text = new Label("It seems like there are no imported savegames for " +
                SavegameManagerState.get().current().getFullName() + " yet.\n");
        v.getChildren().add(text);

        Button importB = new Button("Import savegames");
        importB.setOnAction(e -> {
            GuiImporter.createImporterDialog(SavegameWatcher.ALL.get(
                    SavegameManagerState.get().current()));
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
            ThreadHelper.browse("https://github.com/crschnick/pdx_unlimiter/blob/master/GUIDE.md");
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
