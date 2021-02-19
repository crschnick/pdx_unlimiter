package com.crschnick.pdx_unlimiter.app.util;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.PdxuInstallation;
import com.crschnick.pdx_unlimiter.app.gui.GuiTooltips;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4Tag;
import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Button;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_ANALYZE;
import static com.crschnick.pdx_unlimiter.app.gui.GuiStyle.CLASS_MAP;

public class Eu4SeHelper {

    public static boolean supports(SavegameEntry<?,?> entry, SavegameInfo<?> info) {
        if (!PdxuInstallation.getInstance().isEu4SaveEditorInstalled()) {
            return false;
        }

        if (info.isBinary()) {
            return false;
        }

        return SavegameStorage.EU4 != null && SavegameStorage.EU4.contains(entry);
    }

    public static void open(SavegameEntry<?,?> entry) {
        SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) entry;
        String saveFile = "save_file=" + SavegameStorage.EU4.getSavegameFile(eu4Entry).toString();
        String modsFolder = "mods_folder=" + GameInstallation.EU4.getUserPath().resolve("mod").toString();
        String gameFolder = "game_folder=" + GameInstallation.EU4.getPath().toString();

        try {
            var proc = new ProcessBuilder(
                    PdxuInstallation.getInstance().getEu4SaveEditorLocation()
                            .resolve("bin").resolve("Eu4SaveEditor.bat").toString(), saveFile, modsFolder, gameFolder).start();
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
    }
}
