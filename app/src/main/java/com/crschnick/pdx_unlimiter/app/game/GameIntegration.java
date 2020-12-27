package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.SavedState;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.app.util.ThreadHelper;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class GameIntegration<T, I extends SavegameInfo<T>> {

    public static Eu4Integration EU4;
    public static Hoi4Integration HOI4;
    public static StellarisIntegration STELLARIS;
    public static Ck3Integration CK3;

    private static SimpleObjectProperty<GameIntegration<?, ? extends SavegameInfo>> current = new SimpleObjectProperty<>();

    private static List<GameIntegration<?, ? extends SavegameInfo<?>>> ALL;

    private static SimpleObjectProperty<? extends GameCampaign<?, ? extends SavegameInfo<?>>> globalSelectedCampaign =
            new SimpleObjectProperty<>();

    private static SimpleObjectProperty<? extends GameCampaignEntry<?, ? extends SavegameInfo<?>>> globalSelectedEntry =
            new SimpleObjectProperty<>();


    protected SimpleObjectProperty<GameCampaign<T, I>> selectedCampaign = new SimpleObjectProperty<>();
    protected SimpleObjectProperty<GameCampaignEntry<T, I>> selectedEntry = new SimpleObjectProperty<>();

    public static List<GameIntegration<?, ?>> getAvailable() {
        return ALL;
    }

    public static <T, I extends SavegameInfo<T>> ReadOnlyObjectProperty<GameCampaign<T, I>> globalSelectedCampaignProperty() {
        return (SimpleObjectProperty<GameCampaign<T, I>>) globalSelectedCampaign;
    }

    private static <T, I extends SavegameInfo<T>> SimpleObjectProperty<GameCampaign<T, I>> globalSelectedCampaignPropertyInternal() {
        return (SimpleObjectProperty<GameCampaign<T, I>>) globalSelectedCampaign;
    }

    public static <T, I extends SavegameInfo<T>>
    ReadOnlyObjectProperty<GameCampaignEntry<T, I>> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<GameCampaignEntry<T, I>>) globalSelectedEntry;
    }

    private static <T, I extends SavegameInfo<T>>
    SimpleObjectProperty<GameCampaignEntry<T, I>> globalSelectedEntryPropertyInternal() {
        return (SimpleObjectProperty<GameCampaignEntry<T, I>>) globalSelectedEntry;
    }

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> current() {
        return (GameIntegration<T, I>) current.get();
    }

    public static <T, I extends SavegameInfo<T>, G extends GameIntegration<T, I>> SimpleObjectProperty<G> currentGameProperty() {
        return (SimpleObjectProperty<G>) current;
    }

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> getForInstallation(GameInstallation i) {
        for (var g : ALL) {
            if (g.getInstallation().equals(i)) {
                return (GameIntegration<T, I>) g;
            }
        }
        throw new IllegalArgumentException();
    }

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> getForSavegameCache(SavegameCache<T, I> c) {
        for (var g : ALL) {
            if (g.getSavegameCache().equals(c)) {
                return (GameIntegration<T, I>) g;
            }
        }
        throw new IllegalArgumentException();
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public static void init() {
        ALL = new ArrayList<>();
        SavedState s = SavedState.getInstance();
        if (GameInstallation.EU4 != null) {
            EU4 = new Eu4Integration();
            ALL.add(EU4);
            if (s.getActiveGame().equals(GameInstallation.EU4)) {
                current.set(EU4);
            }
        }
//        if (GameInstallation.HOI4 != null) {
//            HOI4 = new Hoi4Integration();
//            ALL.add(HOI4);
//            if (s.getActiveGame().equals(GameInstallation.HOI4)) {
//                current.set(HOI4);
//            }
//        }

        if (GameInstallation.STELLARIS != null) {
            STELLARIS = new StellarisIntegration();
            ALL.add(STELLARIS);
            if (s.getActiveGame().equals(GameInstallation.STELLARIS)) {
                current.set(STELLARIS);
            }
        }

        if (GameInstallation.CK3 != null) {
            CK3 = new Ck3Integration();
            ALL.add(CK3);
            if (s.getActiveGame().equals(GameInstallation.CK3)) {
                current.set(CK3);
            }
        }

        if (ALL.size() > 0 && current.get() == null) {
            current.set(ALL.get(0));
        }
    }

    public static void reset() {
        if (current() != null) {
            selectIntegration(null);
        }
        ALL.clear();
        EU4 = null;
        CK3 = null;
        STELLARIS = null;
        HOI4 = null;
    }

    private static void unselectCampaignAndEntry() {
        if (current.isNotNull().get()) {
            current().selectedEntry.set(null);
            globalSelectedEntryPropertyInternal().set(null);
            current().selectedCampaign.set(null);
            globalSelectedCampaignPropertyInternal().set(null);
        }
    }

    public static void selectIntegration(GameIntegration<?, ?> newInt) {
        if (current.get() == newInt) {
            return;
        }

        unselectCampaignAndEntry();

        current.set(newInt);
        LoggerFactory.getLogger(GameIntegration.class).debug("Selected integration " + (newInt != null ? newInt.getName() : "null"));
    }

    public static <T, I extends SavegameInfo<T>> void selectCampaign(GameCampaign<T, I> c) {
        if (c == null) {
            unselectCampaignAndEntry();
            LoggerFactory.getLogger(GameIntegration.class).debug("Unselected campaign");
            return;
        }

        if (globalSelectedCampaign.isNotNull().get() && globalSelectedCampaign.get().equals(c)) {
            return;
        }

        Optional<GameIntegration<T, I>> gi = ALL.stream()
                .filter(i -> i.getSavegameCache().getCampaigns().contains(c))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        gi.ifPresentOrElse(v -> {
            selectIntegration(v);
            v.selectedCampaign.set(c);
            globalSelectedCampaignPropertyInternal().set((GameCampaign<Object, SavegameInfo<Object>>) c);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign " + c.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign " + c.getName());
            selectIntegration(null);
        });
    }

    public static <T, I extends SavegameInfo<T>> void selectEntry(GameCampaignEntry<T, I> e) {
        if (globalSelectedEntryPropertyInternal().isEqualTo(e).get()) {
            return;
        }

        if (e == null) {
            if (current.isNotNull().get()) {
                current.get().selectedEntry.set(null);
                globalSelectedEntryPropertyInternal().set(null);
            }
            return;
        }

        Optional<GameIntegration<T, I>> gi = ALL.stream()
                .filter(i -> i.getSavegameCache().contains(e))
                .findFirst()
                .map(v -> (GameIntegration<T, I>) v);
        gi.ifPresentOrElse(v -> {
            selectCampaign(v.getSavegameCache().getCampaign(e));

            v.selectedEntry.set(e);
            globalSelectedEntryPropertyInternal().set((GameCampaignEntry<Object, SavegameInfo<Object>>) e);
            LoggerFactory.getLogger(GameIntegration.class).debug("Selected campaign entry " + e.getName());
        }, () -> {
            LoggerFactory.getLogger(GameIntegration.class).debug("No game integration found for campaign entry " + e.getName());
        });
    }

    public abstract String getName();

    public abstract GameInstallation getInstallation();

    public abstract SavegameWatcher getSavegameWatcher();

    public final Optional<Path> exportCampaignEntry() {
        try {
            var path = getInstallation().getExportTarget(getSavegameCache(), selectedEntry.get());
            getSavegameCache().exportSavegame(selectedEntry.get(), path);
            return Optional.of(path);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return Optional.empty();
        }
    }

    public final void launchCampaignEntry() {
        if (selectedEntry.get() == null) {
            return;
        }

        var e = selectedEntry.get();

        if (!isEntryCompatible(e)) {
            boolean startAnyway = getGuiFactory().displayIncompatibleWarning(e);
            if (!startAnyway) {
                return;
            }
        }

        Optional<Path> p = exportCampaignEntry();
        if (p.isPresent()) {
            try {
                getInstallation().writeLaunchConfig(
                        selectedEntry.get().getName(), selectedCampaign.get().getLastPlayed(), p.get());

                var mods = e.getInfo().getMods().stream()
                        .map(m -> getInstallation().getModForName(m))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                var dlcs = e.getInfo().getDlcs().stream()
                        .map(d -> getInstallation().getDlcForName(d))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                getInstallation().writeDlcLoadFile(mods, dlcs);

                selectedCampaign.get().lastPlayedProperty().setValue(Instant.now());
                getInstallation().startDirectly();
            } catch (Exception ex) {
                ErrorHandler.handleException(ex);
                return;
            }
        }
    }

    public boolean isVersionCompatible(GameCampaignEntry<T, I> entry) {
        return areCompatible(getInstallation().getVersion(), entry.getInfo().getVersion());
    }

    public boolean isEntryCompatible(GameCampaignEntry<T, I> entry) {
        boolean missingMods = entry.getInfo().getMods().stream()
                .map(m -> getInstallation().getModForName(m))
                .anyMatch(Optional::isEmpty);

        boolean missingDlc = entry.getInfo().getDlcs().stream()
                .map(m -> getInstallation().getDlcForName(m))
                .anyMatch(Optional::isEmpty);

        return isVersionCompatible(entry) && !missingMods && !missingDlc;
    }

    public abstract GameGuiFactory<T, I> getGuiFactory();

    public abstract SavegameCache<T, I> getSavegameCache();

    public void openCampaignEntry(GameCampaignEntry<T, I> entry) {
        ThreadHelper.open(getSavegameCache().getPath(entry));
    }
}
