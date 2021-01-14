package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.game.GameCampaignEntry;
import com.crschnick.pdx_unlimiter.app.game.GameIntegration;
import com.crschnick.pdx_unlimiter.app.game.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.gui.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SavegameActions {

    public static boolean isEntryCompatible(GameCampaignEntry<?,?> entry) {
        SavegameManagerState s = SavegameManagerState.get();
        boolean missingMods = entry.getInfo().getMods().stream()
                .map(m -> s.current().getInstallation().getModForName(m))
                .anyMatch(Optional::isEmpty);

        boolean missingDlc = entry.getInfo().getDlcs().stream()
                .map(m -> s.current().getInstallation().getDlcForName(m))
                .anyMatch(Optional::isEmpty);

        return areCompatible(s.current().getInstallation().getVersion(), entry.getInfo().getVersion()) &&
                !missingMods && !missingDlc;
    }

    public static boolean isVersionCompatible(GameCampaignEntry<?, ?> entry) {
        return areCompatible(
                SavegameManagerState.get().current().getInstallation().getVersion(),
                entry.getInfo().getVersion());
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public static <T, I extends SavegameInfo<T>> void openCampaignEntry(GameCampaignEntry<T, I> entry) {
        ThreadHelper.open(SavegameManagerState.get().<T,I>current().getSavegameCache().getPath(entry));
    }

    public static Optional<Path> exportCampaignEntry() {
        SavegameManagerState s = SavegameManagerState.get();
        try {
            var path = s.current().getInstallation().getExportTarget(
                    s.current().getSavegameCache(), s.globalSelectedEntryProperty().get());
            s.current().getSavegameCache().exportSavegame(s.globalSelectedEntryProperty().get(), path);
            return Optional.of(path);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }
    }

    public static <T, I extends SavegameInfo<T>> void moveEntry(
            SavegameCollection<T,I> collection, GameCampaignEntry<T,I> entry) {
        SavegameManagerState s = SavegameManagerState.get();
        s.<T,I>current().getSavegameCache().moveEntry(collection, entry);
        SavegameManagerState.get().selectEntry(entry);
    }

    public static <T, I extends SavegameInfo<T>> Image createImageForEntry(GameCampaignEntry<T,I> entry) {
        @SuppressWarnings("unchecked")
        Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameCache().contains(entry))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        var g = gi.orElseThrow(IllegalArgumentException::new);
        return g.getGuiFactory().tagImage(entry, entry.getInfo().getTag());
    }

    public static void launchCampaignEntry() {
        SavegameManagerState s = SavegameManagerState.get();
        if (s.globalSelectedEntryProperty().get() == null) {
            return;
        }

        var e = s.globalSelectedEntryProperty().get();

        if (!isEntryCompatible(e)) {
            boolean startAnyway = s.current().getGuiFactory().displayIncompatibleWarning(e);
            if (!startAnyway) {
                return;
            }
        }

        Optional<Path> p = exportCampaignEntry();
        if (p.isPresent()) {
            try {
                s.current().getInstallation().writeLaunchConfig(
                        s.globalSelectedEntryProperty().get().getName(),
                        s.globalSelectedCampaignProperty().get().getLastPlayed(), p.get());

                var mods = e.getInfo().getMods().stream()
                        .map(m -> s.current().getInstallation().getModForName(m))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                var dlcs = e.getInfo().getDlcs().stream()
                        .map(d -> s.current().getInstallation().getDlcForName(d))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                s.current().getInstallation().writeDlcLoadFile(mods, dlcs);

                s.globalSelectedCampaignProperty().get().lastPlayedProperty().setValue(Instant.now());
                s.current().getInstallation().startDirectly();
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
            }
        }
    }

    public static void importLatestSavegame() {
        var savegames = SavegameManagerState.get().current().getSavegameWatcher().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        FileImporter.importTargets(Set.of(savegames.get(0)));
    }

    public static void importLatestSavegameDirectly(Consumer<SavegameParser.Status> r) {
        var savegames = SavegameManagerState.get().current().getSavegameWatcher().getSavegames();
        if (savegames.size() == 0) {
            return;
        }

        savegames.get(0).importTarget(r);
    }

    public static <T, I extends SavegameInfo<T>> void meltSavegame(GameCampaignEntry<T,I> e) {
        if (!DialogHelper.showMeltDialog()) {
            return;
        }

        SavegameManagerState s = SavegameManagerState.get();
        TaskExecutor.getInstance().submitTask(() -> {
            s.<T, I>current().getSavegameCache().meltSavegame(e);
        }, true);
    }
}
