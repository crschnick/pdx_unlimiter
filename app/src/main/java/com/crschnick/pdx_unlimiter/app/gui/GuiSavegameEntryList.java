package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiImporter;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.kordamp.ikonli.javafx.FontIcon;

public class GuiSavegameEntryList {

    public static Pane createCampaignEntryList() {
        ListView<Node> grid = GuiListView.createViewOfList(
                SavegameManagerState.get().getShownEntries(),
                GuiSavegameEntry::createSavegameEntryNode,
                SavegameManagerState.get().globalSelectedEntryProperty());
        grid.setOpacity(0.9);
        grid.getStyleClass().add(GuiStyle.CLASS_ENTRY_LIST);

        var ncn = createNoCampaignNode();
        StackPane pane = new StackPane(ncn, grid);
        ncn.prefWidthProperty().bind(pane.widthProperty());
        ncn.prefHeightProperty().bind(pane.heightProperty());
        grid.prefWidthProperty().bind(pane.widthProperty());
        grid.prefHeightProperty().bind(pane.heightProperty());

        grid.setVisible(!SavegameManagerState.get().isStorageEmpty());
        ncn.setVisible(SavegameManagerState.get().isStorageEmpty());
        SavegameManagerState.get().storageEmptyProperty().addListener((c, o, n) -> {
            grid.setVisible(!n);
            ncn.setVisible(n);
        });
        return pane;
    }

    private static Region createNoCampaignNode() {
        VBox v = new VBox();
        Label text = new Label();
        v.getChildren().add(text);
        SavegameManagerState.get().onGameChange(n -> {
            if (n != null) {
                Platform.runLater(() -> {
                    text.setText("It seems like there are no imported savegames for " +
                            SavegameManagerState.get().current().getFullName() + " yet.\n");
                });
            }
        });

        Button importB = new Button("Import savegames");
        importB.setOnAction(e -> {
            GuiImporter.createImporterDialog();
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
        return v;
    }
}
