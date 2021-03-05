package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.game.Ck3Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.Hoi4Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.StellarisInstallation;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.InstallLocationHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public abstract class GameInstallation {

    public static Eu4Installation EU4 = null;
    public static Hoi4Installation HOI4 = null;
    public static Ck3Installation CK3 = null;
    public static StellarisInstallation STELLARIS = null;

    public static Map<Game,GameInstallation> ALL;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected DistributionType distType;
    protected Path userDir;
    protected List<GameMod> mods = new ArrayList<>();
    protected GameVersion version;
    private Path path;
    private Path executable;
    private List<GameDlc> dlcs = new ArrayList<>();

    public GameInstallation(String id, Path path, Path executable) {
        this.path = path;
        if (SystemUtils.IS_OS_WINDOWS) {
            this.executable = getPath().resolve(executable).resolveSibling(executable.getFileName().toString() + ".exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            this.executable = getPath().resolve(executable);
        }
    }

    public static void init() throws Exception {
        Settings s = Settings.getInstance();
        Optional.ofNullable(s.eu4.getValue()).ifPresent(p -> GameInstallation.EU4 = new Eu4Installation(p));
        Optional.ofNullable(s.ck3.getValue()).ifPresent(p -> GameInstallation.CK3 = new Ck3Installation(p));
        Optional.ofNullable(s.hoi4.getValue()).ifPresent(p -> GameInstallation.HOI4 = new Hoi4Installation(p));
        Optional.ofNullable(s.stellaris.getValue()).ifPresent(p -> GameInstallation.STELLARIS = new StellarisInstallation(p));
        ALL = Map.of(Game.EU4, EU4, Game.CK3, CK3, Game.HOI4, HOI4, Game.STELLARIS, STELLARIS);
        for (GameInstallation i : ALL.values()) {
            if (i == null) {
                continue;
            }

            i.loadData();
            try {
                i.initOptional();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public static void reset() {
        ALL = Map.of();
        EU4 = null;
        HOI4 = null;
        STELLARIS = null;
        CK3 = null;
    }

    public List<Path> getAllSavegameDirectories() {
        List<Path> savegameDirs = new ArrayList<>();
        savegameDirs.add(getSavegamesPath());
        savegameDirs.addAll(getDistType().getSavegamePaths());
        return savegameDirs;
    }

    public void initOptional() throws Exception {
        LoggerFactory.getLogger(getClass()).debug("Initializing optional data ...");
        loadDlcs();
        loadMods();
        LoggerFactory.getLogger(getClass()).debug("Finished initializing optional data\n");
    }

    public void startLauncher() {
        getDistType().startLauncher();
    }

    public <T, I extends SavegameInfo<T>> Path getExportTarget(
            SavegameStorage<T, I> cache, SavegameEntry<T, I> e) {
        Path file = getSavegamesPath().resolve(cache.getFileName(e));
        return file;
    }

    private void loadDlcs() throws IOException {
        Files.list(getDlcPath()).forEach(f -> {
            try {
                GameDlc.fromDirectory(f).ifPresent(d -> dlcs.add(d));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    private void loadMods() throws IOException {
        if (!Files.isDirectory(getUserPath().resolve("mod"))) {
            return;
        }

        Files.list(getUserPath().resolve("mod")).forEach(f -> {
            try {
                GameMod.fromFile(f).ifPresent(m -> {
                    if (Files.exists(m.getPath())) mods.add(m);
                    LoggerFactory.getLogger(getClass()).debug("Found mod " + m.getName() +
                            " at " + m.getModFile().toString() + ". Content exists: " + Files.exists(m.getPath()));
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    protected Path replaceVariablesInPath(String value) {
        if (SystemUtils.IS_OS_WINDOWS) {
            value = value.replace("%USER_DOCUMENTS%",
                    InstallLocationHelper.getUserDocumentsPath().toString());
        } else if (SystemUtils.IS_OS_LINUX) {
            value = value.replace("$LINUX_DATA_HOME",
                    InstallLocationHelper.getUserDocumentsPath().toString());
        }
        return Path.of(value);
    }

    public Optional<GameDlc> getDlcForName(String name) {
        return dlcs.stream().filter(d -> d.getName().equals(name)).findAny();
    }

    public DistributionType getDistType() {
        return distType;
    }

    public Path getDlcPath() {
        return getPath().resolve("dlc");
    }

    public abstract void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException;

    public abstract Optional<GameMod> getModForName(String name);

    public abstract void startDirectly();

    public abstract void loadData() throws Exception;

    public Path getLauncherDataPath() {
        return getPath();
    }

    public Path getPath() {
        return path;
    }

    public Path getModBasePath() {
        return path;
    }

    public Path getExecutable() {
        return executable;
    }

    public Path getUserPath() {
        return userDir;
    }

    public Path getSavegamesPath() {
        return getUserPath().resolve("save games");
    }

    public GameVersion getVersion() {
        return version;
    }

    public List<GameMod> getMods() {
        return mods;
    }

    public List<GameDlc> getDlcs() {
        return dlcs;
    }


}
