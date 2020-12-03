package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.achievement.AchievementManager;
import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.savegame.RawSavegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.Savegame;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class GameIntegration<T, I extends SavegameInfo<T>> {

    public static Eu4Integration EU4;
    public static Hoi4Integration HOI4;
    public static StellarisIntegration STELLARIS;
    public static Ck3Integration CK3;

    private static SimpleObjectProperty<GameIntegration<?,? extends SavegameInfo>> current = new SimpleObjectProperty<>();

    private static List<GameIntegration<?,? extends SavegameInfo<?>>> ALL;

    private static SimpleObjectProperty<? extends GameCampaign<?,? extends SavegameInfo<?>>> globalSelectedCampaign =
            new SimpleObjectProperty<>();

    private static SimpleObjectProperty<? extends GameCampaignEntry<?,? extends SavegameInfo<?>>> globalSelectedEntry =
            new SimpleObjectProperty<>();


    protected SimpleObjectProperty<GameCampaign<T,I>> selectedCampaign = new SimpleObjectProperty<>();
    protected SimpleObjectProperty<GameCampaignEntry<T,I>> selectedEntry = new SimpleObjectProperty<>();

    public static boolean init() {
        ALL = new ArrayList<>();
        Settings s = Settings.getInstance();
        if (Settings.getInstance().getEu4().isPresent()) {
            EU4 = new Eu4Integration();
            ALL.add(EU4);
            if (s.getActiveGame().equals(s.getEu4())) {
                current.set(EU4);
            }
        }
        if (Settings.getInstance().getHoi4().isPresent()) {
            HOI4 = new Hoi4Integration();
            ALL.add(HOI4);
            if (s.getActiveGame().equals(s.getHoi4())) {
                current.set(HOI4);
            }
        }

        if (Settings.getInstance().getStellaris().isPresent()) {
            STELLARIS = new StellarisIntegration();
            ALL.add(STELLARIS);
            if (s.getActiveGame().equals(s.getStellaris())) {
                current.set(STELLARIS);
            }
        }

        if (Settings.getInstance().getCk3().isPresent()) {
            CK3 = new Ck3Integration();
            ALL.add(CK3);
            if (s.getActiveGame().equals(s.getCk3())) {
                current.set(CK3);
            }
        }

        if (ALL.size() == 0) {
            return false;
        }

        if (current.get() == null) {
            current.set(ALL.get(0));
        }
        return true;
    }

    public static void reload() {
        if (current() != null) {
            current().selectCampaign(null);
        }
        init();
    }

    public static List<GameIntegration<?, ?>> getAvailable() {
        return ALL;
    }

    public static <T, I extends SavegameInfo<T>> ReadOnlyObjectProperty<GameCampaign<T,I>> globalSelectedCampaignProperty() {
        return (SimpleObjectProperty<GameCampaign<T,I>>) globalSelectedCampaign;
    }

    private static <T, I extends SavegameInfo<T>> SimpleObjectProperty<GameCampaign<T,I>> globalSelectedCampaignPropertyInternal() {
        return (SimpleObjectProperty<GameCampaign<T,I>>) globalSelectedCampaign;
    }

    public static <T, I extends SavegameInfo<T>>
    ReadOnlyObjectProperty<GameCampaignEntry<T,I>> globalSelectedEntryProperty() {
        return (SimpleObjectProperty<GameCampaignEntry<T,I>>) globalSelectedEntry;
    }

    private static <T, I extends SavegameInfo<T>>
    SimpleObjectProperty<GameCampaignEntry<T,I>> globalSelectedEntryPropertyInternal() {
        return (SimpleObjectProperty<GameCampaignEntry<T,I>>) globalSelectedEntry;
    }

    public static <T, I extends SavegameInfo<T>> GameIntegration<T,I> current() {
        return (GameIntegration<T,I>) current.get();
    }

    public static <T, I extends SavegameInfo<T>, G extends GameIntegration<T,I>> SimpleObjectProperty<G> currentGameProperty() {
        return (SimpleObjectProperty<G>) current;
    }

    public static GameIntegration<?, ?> getForInstallation(GameInstallation i) {
        for (var g : ALL) {
            if (g.getInstallation().equals(i)) {
                return g;
            }
        }
        throw new IllegalArgumentException();
    }

    public static GameIntegration<?, ?> getForSavegameCache(SavegameCache c) {
        for (var g : ALL) {
            if (g.getSavegameCache().equals(c)) {
                return g;
            }
        }
        throw new IllegalArgumentException();
    }

    public static void selectIntegration(GameIntegration<?, ?> newInt) {
        if (current.get() == newInt) {
            return;
        }

        current().selectCampaign(null);
        current.set(newInt);
        Settings.getInstance().updateActiveGame(current().getInstallation().getPath());
        LoggerFactory.getLogger(GameIntegration.class).debug("Selected integration " + (newInt != null ? newInt.getName() : "null"));
    }

    public abstract String getName();

    public abstract GameInstallation getInstallation();

    public abstract AchievementManager getAchievementManager();

    public final Optional<Path> exportCampaignEntry() {
        try {
            var path = getInstallation().getSavegamesPath().resolve(getSavegameCache().getFileName(selectedEntry.get()));
            getSavegameCache().exportSavegame(selectedEntry.get(),
                    path);
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

        Optional<Path> p = exportCampaignEntry();
        if (p.isPresent()) {
            try {
                getInstallation().writeLaunchConfig(
                        selectedEntry.get().getName(), selectedCampaign.get().getLastPlayed(), p.get());
            } catch (IOException ioException) {
                ErrorHandler.handleException(ioException);
                return;
            }
        }
        selectedCampaign.get().lastPlayedProperty().setValue(Instant.now());
        getInstallation().start();
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public boolean isVersionCompatible(GameCampaignEntry<T,I> entry) {
        return areCompatible(getInstallation().getVersion(), entry.getInfo().getVersion());
    }

    public boolean isEntryCompatible(GameCampaignEntry<T,I> entry) {
        boolean missingMods = entry.getInfo().getMods().stream()
                .map(m -> getInstallation().getModForName(m))
                .anyMatch(Optional::isEmpty);

        boolean missingDlc = entry.getInfo().getDlcs().stream()
                .map(m -> getInstallation().getDlcForName(m))
                .anyMatch(Optional::isEmpty);

        return isVersionCompatible(entry) && !missingMods && !missingDlc;
    }

    public abstract GameGuiFactory<T,I> getGuiFactory();

    public abstract SavegameCache<? extends RawSavegame, ? extends Savegame, T, I> getSavegameCache();

    public void openCampaignEntry(GameCampaignEntry<T,I> entry) {
        try {
            Desktop.getDesktop().open(getSavegameCache().getPath(entry).toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectCampaign(GameCampaign<T,I> c) {
        if (this.selectedCampaign.get() == c) {
            return;
        }

        this.selectedEntry.set(null);
        globalSelectedEntryPropertyInternal().set(null);
        this.selectedCampaign.set(c);
        globalSelectedCampaignPropertyInternal().set((GameCampaign<Object, SavegameInfo<Object>>) c);
        LoggerFactory.getLogger(GameIntegration.class).debug("Selecting campaign " + (c != null ? c.getName() : "null"));
    }

    public void selectEntry(GameCampaignEntry<T,I> e) {
        if (this.selectedEntry.get() == e) {
            return;
        }

        if (e != null) {
            this.selectedCampaign.set(getSavegameCache().getCampaign(e));
            globalSelectedCampaignPropertyInternal().set(
                    (GameCampaign<Object, SavegameInfo<Object>>) getSavegameCache().getCampaign(e));
        }
        this.selectedEntry.set(e);
        globalSelectedEntryPropertyInternal().set((GameCampaignEntry<Object, SavegameInfo<Object>>) e);

        LoggerFactory.getLogger(GameIntegration.class).debug("Selecting campaign entry " + (e != null ? e.getName() : "null"));
    }
}
