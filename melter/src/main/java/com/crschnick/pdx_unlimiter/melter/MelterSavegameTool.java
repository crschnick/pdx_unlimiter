package com.crschnick.pdx_unlimiter.melter;


import com.crschnick.pdx_unlimiter.app.core.SavegameTool;
import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.gui.PdxuStyle;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameContext;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.gui_utils.GuiAlertHelper;
import javafx.scene.control.Alert;

import java.nio.file.Path;

public class MelterSavegameTool implements SavegameTool {

    @Override
    public boolean shouldShow(SavegameEntry<?, ?> entry, SavegameInfo<?> info) {
        return info.isBinary();
    }

    @Override
    public String getIconId() {
        return "mdi-stove";
    }

    @Override
    public String getTooltip() {
        return "Melt savegame (Convert to Non-Ironman)";
    }

    private boolean showMeltDialog() {
        return GuiAlertHelper.showBlockingAlert(PdxuStyle.get(), alert -> {
            alert.setAlertType(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Melt savegame");
            alert.setHeaderText("""
                Do you want to convert the selected savegame into a non-ironman savegame using the Rakaly melter?
                """);
            alert.setContentText("""
                The original savegame will not get modified.""");
        }).map(b -> b.getButtonData().isDefaultButton()).orElse(false);
    }

    @Override
    public void onClick(SavegameEntry<?, ?> e) {
        if (!showMeltDialog()) {
            return;
        }

        TaskExecutor.getInstance().submitTask(() -> {
            SavegameContext.withSavegame(e, ctx -> {
                Path meltedFile;
                try {
                    meltedFile = RakalyMelter.meltSavegame(ctx.getStorage().getSavegameFile(e));
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                    return;
                }
                var folder = ctx.getStorage().getOrCreateFolder("Melted savegames");
                folder.ifPresent(f -> {
                    ctx.getStorage().importSavegame(meltedFile, null, true, null, f);
                });
            });
        }, true);
    }
}
