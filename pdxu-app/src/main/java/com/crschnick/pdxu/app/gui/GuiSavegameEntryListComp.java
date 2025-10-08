package com.crschnick.pdxu.app.gui;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.gui.dialog.GuiImporter;
import com.crschnick.pdxu.app.info.SavegameInfo;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.AllArgsConstructor;
import org.kordamp.ikonli.javafx.FontIcon;

@AllArgsConstructor
public class GuiSavegameEntryListComp<T, I extends SavegameInfo<T>> extends SimpleComp {

    private final SavegameManagerState<T, I> savegameManagerState;

    private Region createNoCampaignNode() {
        VBox v = new VBox();
        Label text = new Label();
        v.getChildren().add(text);

        Button importB = new Button(AppI18n.get("importSavegames"));
        importB.setOnAction(e -> {
            GuiImporter.createImporterDialog(savegameManagerState.getGame());
            e.consume();
        });
        importB.setGraphic(new FontIcon());
        importB.getStyleClass().add(GuiStyle.CLASS_IMPORT);
        v.getChildren().add(importB);

        v.getStyleClass().add(GuiStyle.CLASS_NO_CAMPAIGN);
        v.setFillWidth(true);
        v.setSpacing(10);
        v.setAlignment(Pos.CENTER);
        return v;
    }

    @Override
    protected Region createSimple() {
        Region grid = new GuiListViewComp<>(
                savegameManagerState.getShownEntries(),
                savegameEntry -> new GuiSavegameEntryComp<>(savegameEntry, savegameManagerState),
                true)
                .createRegion();
        grid.setOpacity(0.9);
        grid.getStyleClass().add(GuiStyle.CLASS_ENTRY_LIST);

        var ncn = createNoCampaignNode();
        StackPane pane = new StackPane(ncn, grid);
        ncn.prefWidthProperty().bind(pane.widthProperty());
        ncn.prefHeightProperty().bind(pane.heightProperty());
        grid.prefWidthProperty().bind(pane.widthProperty());
        grid.prefHeightProperty().bind(pane.heightProperty());

        grid.setVisible(!savegameManagerState.isStorageEmpty());
        ncn.setVisible(savegameManagerState.isStorageEmpty());
        savegameManagerState.storageEmptyProperty().addListener((c, o, n) -> {
            grid.setVisible(!n);
            ncn.setVisible(n);
        });
        grid.setAccessibleText("Campaign savegames");
        return pane;
    }
}
