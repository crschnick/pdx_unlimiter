package com.crschnick.pdx_unlimiter.app.gui;

import com.crschnick.pdx_unlimiter.app.PdxuApp;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.FileImporter;
import javafx.scene.Node;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

public class GuiLayout {

    private static BorderPane layout;

    public static Pane createLayout() {
        layout = new BorderPane();
        var menu = GuiMenuBar.createMenu();
        layout.setTop(menu);
        Pane p = new Pane();
        layout.setBottom(p);
        GuiStatusBar.createStatusBar(p);

        var entryList = GuiGameCampaignEntryList.createCampaignEntryList();
        layout.setCenter(entryList);

        var campaignList = GuiGameCampaignList.createCampaignList();
        layout.setLeft(campaignList);


        layout.setOnDragOver(event -> {
            if (event.getGestureSource() != layout
                    && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        layout.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            FileImporter.addToImportQueue(db.getFiles().stream().map(File::toPath).collect(Collectors.toList()));
            event.setDropCompleted(true);
            event.consume();
        });
        setFontSize(Settings.getInstance().getFontSize());

        StackPane stack = new StackPane(new Pane(), layout);
        GameIntegration.currentGameProperty().addListener((c, o, n) -> {
            if (n != null) {
                stack.getChildren().set(0, n.getGuiFactory().background());
                try {
                    menu.setOpacity(0.95);
                    stack.styleProperty().set("-fx-font-family: " + n.getGuiFactory().font().getName() + ";");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return stack;
    }

    public static void setFontSize(int size) {
        if (layout != null) {
            layout.styleProperty().setValue("-fx-font-size: " + size + "pt;");
        }
    }
}
