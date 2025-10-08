package com.crschnick.pdxu.app.gui;

import atlantafx.base.theme.Styles;
import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.comp.base.IconButtonComp;
import com.crschnick.pdxu.app.comp.base.InputGroupComp;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.gui.dialog.GuiImporter;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;
import com.crschnick.pdxu.app.util.EditorProvider;
import com.crschnick.pdxu.app.util.ThreadHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import lombok.AllArgsConstructor;

import java.util.List;

import static com.crschnick.pdxu.app.gui.GuiStyle.CLASS_CAMPAIGN_LIST;

@AllArgsConstructor
public class GuiSavegameCollectionListComp<T, I extends SavegameInfo<T>> extends SimpleComp {

    private final SavegameManagerState<T,I> savegameManagerState;

    private Comp<?> createEditButton() {
        var b = new IconButtonComp("mdi2c-comment-edit-outline", () -> {
            EditorProvider.get().browseExternalFile(savegameManagerState.getGame());
        });
        b.apply(struc -> {
            struc.get().getStyleClass().remove(Styles.FLAT);
            struc.get().minWidthProperty().bind(struc.get().heightProperty());
        });
        return b;
    }

    private Comp<?> createLaunchButton() {
        var b = new IconButtonComp("mdi-play", () -> {
            GameDistLauncher.startLauncher(savegameManagerState.getGame());
        });
        b.apply(struc -> {
            struc.get().getStyleClass().remove(Styles.FLAT);
            struc.get().minWidthProperty().bind(struc.get().heightProperty());
        });
        return b;
    }

    private Comp<?> createImportButton() {
        var b = new IconButtonComp("mdi-import", () -> {
            GuiImporter.createImporterDialog(savegameManagerState.getGame());
        });
        b.apply(struc -> {
            struc.get().getStyleClass().remove(Styles.FLAT);
            struc.get().minWidthProperty().bind(struc.get().heightProperty());
        });
        return b;
    }

    private Region createTopBar() {
        var filterProperty = new SimpleStringProperty();
        filterProperty.addListener((observable, oldValue, newValue) -> {
            ThreadHelper.runAsync(() -> {
                savegameManagerState.getFilter().filterProperty().set(newValue);
            });
        });
        var filter = new GuiFilterComp(filterProperty).hgrow();
        var launchButton = createLaunchButton();
        var importButton = createImportButton();
        var editButton = createEditButton();
        var group = new InputGroupComp(List.of(launchButton, editButton, filter, importButton));
        group.setMergeComps(false);
        group.setHeightReference(filter);
        group.apply(struc -> {
            struc.get().setSpacing(4);
            struc.get().setPadding(new Insets(8, 8, 0, 8));
            struc.get().setAlignment(Pos.CENTER);
        });
        return group.createRegion();
    }

    @Override
    protected Region createSimple() {
        Region list = new GuiListViewComp<>(
                savegameManagerState.getShownCollections(),
                c -> new GuiSavegameCampaignComp<>(c, () -> savegameManagerState.selectCollectionAsync(c)),
                false)
                .createRegion();
        list.getStyleClass().add(CLASS_CAMPAIGN_LIST);
        list.setAccessibleText("Campaign list");
        list.setPrefWidth(300);

        var top = createTopBar();
        var box = new VBox(top, list);
        top.prefWidthProperty().bind(box.widthProperty());
        VBox.setVgrow(list, Priority.ALWAYS);
        return box;
    }
}
