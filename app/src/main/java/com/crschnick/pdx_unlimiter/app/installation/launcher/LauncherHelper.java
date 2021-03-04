package com.crschnick.pdx_unlimiter.app.installation.launcher;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.gui.dialog.GuiIncompatibleWarning;
import com.crschnick.pdx_unlimiter.app.installation.GameDlc;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameActions;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.SavegameHelper;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LauncherHelper {


    public static <T, I extends SavegameInfo<T>> void launchCampaignEntry(SavegameEntry<T,I> e) {
        SavegameHelper.withSavegame(e, ctx -> {
            var gi = ctx.getIntegration();
            if (!SavegameActions.isEntryCompatible(e)) {
                boolean startAnyway = GuiIncompatibleWarning.showIncompatibleWarning()
                if (!startAnyway) {
                    return;
                }
            }

            Optional<Path> p = SavegameActions.exportSavegame(e);
            if (p.isPresent()) {
                try {
                    gi.getInstallation().writeLaunchConfig(e.getName(), ctx.getCollection().getLastPlayed(), p.get());

                    var mods = e.getInfo().getMods().stream()
                            .map(m -> gi.getInstallation().getModForName(m))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    var dlcs = e.getInfo().getDlcs().stream()
                            .map(d -> gi.getInstallation().getDlcForName(d))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    gi.getInstallation().writeDlcLoadFile(mods, dlcs);

                    ctx.getCollection().lastPlayedProperty().setValue(Instant.now());
                    ctx.getIntegration().getInstallation().startDirectly();
                } catch (Exception ex) {
                    ErrorHandler.handleException(ex);
                }
            }
        });
    }

    static List<GameMod> getEnabledMods(GameInstallation installation) throws IOException {
        var file = installation.getUserPath().resolve("dlc_load.json");
        var node = JsonHelper.read(file);
        return StreamSupport.stream(node.get("enabled_mods").spliterator(), false)
                .map(m -> installation.getModForName(m.textValue()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    static void writeStellarisDlcLoadFile(
            GameInstallation installation, List<GameDlc> dlcs) throws IOException {
        var existingMods = getEnabledMods(installation);
        writeDlcLoadFile(installation, existingMods, dlcs);
    }

    static void writeDlcLoadFile(GameInstallation installation, List<GameMod> mods, List<GameDlc> dlcs) throws IOException {
        var file = installation.getUserPath().resolve("dlc_load.json");
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        n.putArray("enabled_mods").addAll(mods.stream()
                .map(d -> FilenameUtils.separatorsToUnix
                        (installation.getUserPath().relativize(d.getModFile()).toString()))
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        n.putArray("disabled_dlcs").addAll(installation.getDlcs().stream()
                .filter(d -> d.isExpansion() && !dlcs.contains(d))
                .map(d -> FilenameUtils.separatorsToUnix(
                        installation.getPath().relativize(d.getInfoFilePath()).toString()))
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        JsonHelper.write(n, file);
    }
}
