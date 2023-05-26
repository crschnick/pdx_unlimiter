package com.crschnick.pdxu.app.util.integration;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.PdxuInstallation;
import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.savegame.SavegameActions;
import com.crschnick.pdxu.app.savegame.SavegameEntry;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.app.util.ThreadHelper;
import com.crschnick.pdxu.model.eu4.Eu4Tag;

import java.io.IOException;
import java.nio.file.Files;

public class Eu4SeHelper {

    public static boolean isSupported() {
        return false;
    }

    public static boolean shouldShowButton(SavegameEntry<?, ?> entry, SavegameInfo<?> info) {
        if (!isSupported()) {
            return false;
        }

        if (info.getData().isBinary()) {
            return false;
        }

        return SavegameStorage.ALL.get(Game.EU4) != null && SavegameStorage.ALL.get(Game.EU4).contains(entry);
    }

    public static void open(SavegameEntry<?, ?> entry) {
        ThreadHelper.create("eu4se", true, () -> {
            // Create mod dir in case no mods are installed
            var modDir = GameInstallation.ALL.get(Game.EU4).getUserDir().resolve("mod");
            if (!Files.exists(modDir)) {
                try {
                    Files.createDirectory(modDir);
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                    return;
                }
            }

            @SuppressWarnings("unchecked")
            SavegameEntry<Eu4Tag, Eu4SavegameInfo> eu4Entry = (SavegameEntry<Eu4Tag, Eu4SavegameInfo>) entry;
            String saveFile = "\"--save_file=" + SavegameStorage.ALL.get(Game.EU4).getSavegameFile(eu4Entry).toString() + "\"";
            String gameFolder = "\"--game_folder=" + GameInstallation.ALL.get(Game.EU4).getInstallDir().toString() + "\"";
            String overwrite = "--override=true";

            try {
                SavegameStorage.<Eu4Tag, Eu4SavegameInfo>get(Game.EU4).invalidateSavegameInfo(eu4Entry);
                var proc = new ProcessBuilder(
                        PdxuInstallation.getInstance().getJavaExecutableLocation().toString(),
                        "-jar",
                        PdxuInstallation.getInstance().getResourceDir().resolve("bin").resolve("OsaSaveEditor.jar").toString(),
                        saveFile, gameFolder, overwrite
                )
                        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                        .start();
                var out = proc.getErrorStream().readAllBytes();
                var exit = proc.waitFor();
                if (exit != 0) {
                    throw new IOException(new String(out));
                }
                SavegameActions.reloadSavegame(eu4Entry);
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }).start();
    }
}
