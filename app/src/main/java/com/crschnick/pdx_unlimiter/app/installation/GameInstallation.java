package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.dist.GameDistType;
import com.crschnick.pdx_unlimiter.app.installation.dist.PdxLauncherDist;
import com.crschnick.pdx_unlimiter.app.installation.dist.SteamDist;
import com.crschnick.pdx_unlimiter.app.installation.game.Ck3Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.Hoi4Installation;
import com.crschnick.pdx_unlimiter.app.installation.game.StellarisInstallation;
import com.crschnick.pdx_unlimiter.app.lang.Language;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.OsHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class GameInstallation {

    public static final BidiMap<Game, GameInstallation> ALL = new DualHashBidiMap<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<GameMod> mods = new ArrayList<>();
    private final Path path;
    private final List<GameDlc> dlcs = new ArrayList<>();
    private GameDistType distType;
    private Path userDir;
    private GameVersion version;
    private Language language;
    private Path executable;

    public GameInstallation(Path path, Path executable) {
        this.path = path;
        if (SystemUtils.IS_OS_WINDOWS) {
            this.executable = getPath().resolve(executable).resolveSibling(executable.getFileName().toString() + ".exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            this.executable = getPath().resolve(executable);
        }
    }

    public static void initTemporary(Game game, GameInstallation install) throws
            IOException, InvalidInstallationException {
        var oldInstall = ALL.get(game);

        ALL.put(game, install);
        try {
            install.loadData();
        } finally {
            if (oldInstall == null) {
                ALL.remove(game);
            } else {
                ALL.put(game, oldInstall);
            }
        }
    }

    public static void init() {
        Settings s = Settings.getInstance();
        Optional.ofNullable(s.eu4.getValue()).ifPresent(
                p -> ALL.put(Game.EU4, new Eu4Installation(p)));
        Optional.ofNullable(s.ck3.getValue()).ifPresent(
                p -> ALL.put(Game.CK3, new Ck3Installation(p)));
        Optional.ofNullable(s.hoi4.getValue()).ifPresent(
                p -> ALL.put(Game.HOI4, new Hoi4Installation(p)));
        Optional.ofNullable(s.stellaris.getValue()).ifPresent(
                p -> ALL.put(Game.STELLARIS, new StellarisInstallation(p)));
        for (Game g : Game.values()) {
            if (!ALL.containsKey(g)) {
                continue;
            }

            var i = ALL.get(g);
            try {
                i.loadData();
                i.initOptional();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public static void reset() {
        ALL.clear();
    }

    public List<Path> getAllSavegameDirectories() {
        List<Path> savegameDirs = new ArrayList<>();
        savegameDirs.add(getSavegamesPath());
        savegameDirs.addAll(getDistType().getAdditionalSavegamePaths());
        return savegameDirs;
    }

    public void initOptional() throws Exception {
        LoggerFactory.getLogger(getClass()).debug("Initializing optional data ...");
        loadDlcs();
        loadMods();
        LoggerFactory.getLogger(getClass()).debug("Finished initializing optional data\n");
    }

    public <T, I extends SavegameInfo<T>> Path getExportTarget(SavegameEntry<T, I> e) {
        return getSavegamesPath().resolve(SavegameStorage.get(
                ALL.inverseBidiMap().get(this)).getFileSystemCompatibleName(e, false));
    }

    private void loadDlcs() throws IOException {
        if (!Files.isDirectory(getDlcPath())) {
            return;
        }

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
                            " at " + m.getModFile().toString() + ". Content exists: " + Files.exists(m.getPath()) +
                            ". Legacy: " + m.isLegacyArchive());
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    protected Path replaceVariablesInPath(String value) {
        if (SystemUtils.IS_OS_WINDOWS) {
            value = value.replace("%USER_DOCUMENTS%",
                    OsHelper.getUserDocumentsPath().toString());
        } else if (SystemUtils.IS_OS_LINUX) {
            value = value.replace("$LINUX_DATA_HOME",
                    OsHelper.getUserDocumentsPath().toString());
        }
        return Path.of(value);
    }

    public Optional<GameDlc> getDlcForName(String name) {
        return dlcs.stream().filter(d -> d.getName().equals(name)).findAny();
    }

    public GameDistType getDistType() {
        return distType;
    }

    public Path getDlcPath() {
        return getPath().resolve("dlc");
    }

    public abstract void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException;

    public abstract Optional<GameMod> getModForName(String name);

    public abstract void startDirectly() throws IOException;

    public void loadData() throws IOException, InvalidInstallationException {
        Game g = ALL.inverseBidiMap().get(this);
        LoggerFactory.getLogger(getClass()).debug("Initializing " + g.getAbbreviation() + " installation ...");
        if (!Files.isRegularFile(getExecutable())) {
            throw new InvalidInstallationException("EXECUTABLE_NOT_FOUND", g.getAbbreviation(), getExecutable().toString());
        }

        var ls = getLauncherDataPath().resolve("launcher-settings.json");
        if (!Files.exists(ls)) {
            throw new InvalidInstallationException("LAUNCHER_SETTINGS_NOT_FOUND", g.getAbbreviation());
        }

        JsonNode node = JsonHelper.read(ls);
        try {
            this.userDir = determineUserDir(node);
            logger.debug(g.getAbbreviation() + " user dir: " + this.userDir);
            if (!Files.exists(this.userDir)) {
                throw new InvalidInstallationException(
                        "GAME_DATA_PATH_DOES_NOT_EXIST", g.getAbbreviation(), this.userDir.toString());
            }

            this.version = determineVersion(node);
            logger.debug(g.getAbbreviation() + " version: " + this.version);
            this.distType = determineDistType(node);
            logger.debug(g.getAbbreviation() + " distribution type: " + this.distType.getName());
            this.language = determineLanguage();
            logger.debug(g.getAbbreviation() + " language: " +
                    (this.language != null ? this.language.getDisplayName() : "unknown"));
            LoggerFactory.getLogger(getClass()).debug("Finished initialization");
        } catch (InvalidInstallationException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidInstallationException(e);
        }
    }

    protected Path determineUserDir(JsonNode node) throws InvalidInstallationException {
        Game g = ALL.inverseBidiMap().get(this);
        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new InvalidInstallationException("GAME_DATA_PATH_NOT_FOUND", g.getAbbreviation()))
                .textValue();
        return replaceVariablesInPath(value);
    }

    protected abstract GameVersion determineVersion(JsonNode node);

    private GameDistType determineDistType(JsonNode node) throws IOException {
        String platform = node.required("distPlatform").textValue();
        GameDistType d;
        if (platform.equals("steam")) {
            // Trim the id because sometimes it contains trailing new lines!
            var id = Files.readString(getSteamAppIdFile()).trim();
            d = new SteamDist(Integer.parseInt(id), this);
        } else {
            d = new PdxLauncherDist(this);
        }
        return d;
    }

    protected abstract Language determineLanguage() throws Exception;

    public Path getSteamAppIdFile() {
        return getPath().resolve("steam_appid.txt");
    }

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

    public Language getLanguage() {
        return language;
    }
}
