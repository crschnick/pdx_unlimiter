package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.PathChoiceComp;
import com.crschnick.pdxu.app.comp.base.TileButtonComp;
import com.crschnick.pdxu.app.gui.dialog.GuiSavegameIO;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.OptionsBuilder;
import com.crschnick.pdxu.app.savegame.SavegameStorageIO;

import java.nio.file.Path;
import java.util.Optional;

public class StorageCategory extends AppPrefsCategory {

    @Override
    public String getId() {
        return "storage";
    }

    @Override
    protected LabelGraphic getIcon() {
        return new LabelGraphic.IconGraphic("mdi2d-database-outline");
    }

    public Comp<?> create() {
        var prefs = AppPrefs.get();
        var builder = new OptionsBuilder();
        builder.addTitle("storage")
                .sub(new OptionsBuilder()
                        .pref(prefs.storageDirectory)
                        .addComp(new PathChoiceComp(prefs.storageDirectory, "storageDirectory", true).maxWidth(600))
                        .spacer(19)
                        .addComp(
                                new TileButtonComp("exportStorage", "exportStorageDescription", "mdi-export", e -> {
                                    Optional<Path> path = GuiSavegameIO.showExportDialog();
                                    path.ifPresent(p -> {
                                        SavegameStorageIO.exportSavegameStorage(p);
                                    });
                                })
                                        .maxWidth(2000),
                                null)
                );
        return builder.buildComp();
    }
}
