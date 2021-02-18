package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.TaskExecutor;
import com.crschnick.pdx_unlimiter.app.editor.EditTarget;
import com.crschnick.pdx_unlimiter.app.editor.Editor;
import com.crschnick.pdx_unlimiter.app.editor.StorageEditTarget;
import com.crschnick.pdx_unlimiter.app.gui.dialog.DialogHelper;
import com.crschnick.pdx_unlimiter.app.installation.GameIntegration;
import com.crschnick.pdx_unlimiter.app.util.SavegameInfoHelper;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameParser;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SavegameActions {

    public static <T, I extends SavegameInfo<T>> Optional<Path> exportToTemp(SavegameEntry<T,I> entry) {
        return Optional.ofNullable(SavegameInfoHelper.createWithIntegration(entry, gi -> {
            var sc = gi.getSavegameStorage();
            var out = FileUtils.getTempDirectory().toPath().resolve(sc.getFileName(entry));
            try {
                sc.copySavegameTo(entry, out);
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                return null;
            }
            return out;
        }));
    }

    public static boolean isEntryCompatible(SavegameEntry<?, ?> entry) {
        return SavegameInfoHelper.withInfo(entry, (info, gi) -> {
            var ins= gi.getInstallation();
            boolean missingMods = info.getMods().stream()
                    .map(ins::getModForName)
                    .anyMatch(Optional::isEmpty);

            boolean missingDlc = info.getDlcs().stream()
                    .map(ins::getDlcForName)
                    .anyMatch(Optional::isEmpty);

            return areCompatible(ins.getVersion(), info.getVersion()) &&
                    !missingMods && !missingDlc;
        }).orElse(false);
    }

    public static boolean isVersionCompatible(SavegameInfo<?> info) {
        return areCompatible(
                SavegameManagerState.get().current().getInstallation().getVersion(),
                info.getVersion());
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public static <T, I extends SavegameInfo<T>> void openCampaignEntry(SavegameEntry<T, I> entry) {
        ThreadHelper.open(SavegameManagerState.<T, I>get().current().getSavegameStorage().getPath(entry));
    }

    public static Optional<Path> exportCampaignEntry() {
        var s = SavegameManagerState.get();
        try {
            var path = s.current().getInstallation().getExportTarget(
                    s.current().getSavegameStorage(), s.globalSelectedEntryProperty().get());
            s.current().getSavegameStorage().copySavegameTo(s.globalSelectedEntryProperty().get(), path);
            return Optional.of(path);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }
    }

    public static <T, I extends SavegameInfo<T>> void moveEntry(
            SavegameCollection<T, I> collection, SavegameEntry<T, I> entry) {
        var s = SavegameManagerState.<T, I>get();
        s.current().getSavegameStorage().moveEntryAsync(collection, entry);
    }

    public static <T, I extends SavegameInfo<T>> Image createImageForEntry(SavegameEntry<T, I> entry) {
        @SuppressWarnings("unchecked")
        Optional<GameIntegration<T, I>> gi = GameIntegration.ALL.stream()
                .filter(i -> i.getSavegameStorage().contains(entry))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        var g = gi.orElseThrow(IllegalArgumentException::new);
        return g.getGuiFactory().tagImage(entry.getInfo(), entry.getInfo().getTag());
    }

    public static void launchCampaignEntry() {
        var s = SavegameManagerState.get();
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

    public static <T, I extends SavegameInfo<T>> void meltSavegame(SavegameEntry<T, I> e) {
        if (!DialogHelper.showMeltDialog()) {
            return;
        }

        SavegameManagerState<T, I> s = SavegameManagerState.get();
        TaskExecutor.getInstance().submitTask(() -> {
            s.<T, I>current().getSavegameStorage().meltSavegame(e);
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void editSavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            var in = SavegameStorage.getForSavegame(e).getSavegameFile(e);
            var target = EditTarget.create(in);
            target.ifPresent(t -> {
                var storageTarget = new StorageEditTarget<>(SavegameStorage.getForSavegame(e), e, t);
                Editor.createNewEditor(storageTarget);
            });
        }, true);
    }

    public static <T, I extends SavegameInfo<T>> void copySavegame(SavegameEntry<T, I> e) {
        TaskExecutor.getInstance().submitTask(() -> {
            var sgs = SavegameStorage.getForSavegame(e);
            var in = sgs.getSavegameFile(e);
            sgs.importSavegame(in, "Copy of " + e.getName(), false, sgs.getSavegameCollection(e));
        }, true);
    }
}
