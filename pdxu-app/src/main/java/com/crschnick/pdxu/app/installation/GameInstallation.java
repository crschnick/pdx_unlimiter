package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.installation.dist.GameDist;
import com.crschnick.pdxu.app.lang.Language;
import com.crschnick.pdxu.app.util.OsHelper;
import com.crschnick.pdxu.model.GameVersion;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GameInstallation {

    public static final BidiMap<Game, GameInstallation> ALL = new DualHashBidiMap<>();
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final GameInstallType type;
    private final GameDist dist;

    private final List<GameDlc> dlcs = new ArrayList<>();
    private final List<GameMod> mods = new ArrayList<>();
    private final List<GameMod> enabledMods = new ArrayList<>();

    private Path userDir;
    private GameVersion version;
    private Language language;

    public GameInstallation(GameInstallType type, GameDist dist) {
        this.type = type;
        this.dist = dist;
    }

    public static void initTemporary(Game game, GameInstallation install) throws InvalidInstallationException {
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
                p -> ALL.put(Game.EU4, new GameInstallation(Game.EU4.getInstallType(), p)));
        Optional.ofNullable(s.ck3.getValue()).ifPresent(
                p -> ALL.put(Game.CK3, new GameInstallation(Game.CK3.getInstallType(), p)));
        Optional.ofNullable(s.hoi4.getValue()).ifPresent(
                p -> ALL.put(Game.HOI4, new GameInstallation(Game.HOI4.getInstallType(), p)));
        Optional.ofNullable(s.stellaris.getValue()).ifPresent(
                p -> ALL.put(Game.STELLARIS, new GameInstallation(Game.STELLARIS.getInstallType(), p)));
        Optional.ofNullable(s.ck2.getValue()).ifPresent(
                p -> ALL.put(Game.CK2, new GameInstallation(Game.CK2.getInstallType(), p)));
        Optional.ofNullable(s.vic2.getValue()).ifPresent(
                p -> ALL.put(Game.VIC2, new GameInstallation(Game.VIC2.getInstallType(), p)));
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
        savegameDirs.add(getSavegamesDir());
        savegameDirs.addAll(dist.getAdditionalSavegamePaths());
        return savegameDirs;
    }

    public void initOptional() throws Exception {
        LoggerFactory.getLogger(getClass()).debug("Initializing optional data ...");
        loadDlcs();
        loadMods();
        LoggerFactory.getLogger(getClass()).debug("Finished initializing optional data\n");
    }

    private void loadDlcs() throws IOException {
        if (!Files.isDirectory(type.getDlcPath(getInstallDir()))) {
            return;
        }

        Files.list(type.getDlcPath(getInstallDir())).forEach(f -> {
            try {
                GameDlc.fromDirectory(f).ifPresent(d -> dlcs.add(d));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    private void loadMods() throws IOException {
        if (!Files.isDirectory(getUserDir().resolve("mod"))) {
            return;
        }

        Files.list(getUserDir().resolve("mod")).forEach(f -> {
            try {
                GameMod.fromFile(f).ifPresent(m -> {
                    var path = m.getPath().isAbsolute() ? m.getPath() : getUserDir().resolve(m.getPath());
                    if (Files.exists(path)) {
                        mods.add(m);
                    }
                    logger.debug("Found mod " + m.getName() +
                            " at " + m.getModFile().toString() + ". Content exists: " + Files.exists(path) +
                            ". Legacy: " + m.isLegacyArchive());
                });
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    public Optional<GameDlc> getDlcForName(String name) {
        return dlcs.stream().filter(d -> d.getName().equals(name)).findAny();
    }

    public GameDist getDist() {
        return dist;
    }

    public Optional<GameMod> getModForFileName(String fn) {
        return getMods().stream().filter(m -> type.getModFileName(userDir, m).equals(fn)).findAny();
    }

    public Optional<GameMod> getModForSavegameId(String id) {
        return getMods().stream().filter(m -> type.getModSavegameId(userDir, m).equals(id)).findAny();
    }

    private void loadEnabledMods() throws Exception {
        logger.debug("Loading enabled mods ...");
        enabledMods.clear();
        type.getEnabledMods(getInstallDir(), userDir).forEach(s -> {
            var mod = getModForFileName(s);
            mod.ifPresentOrElse(m -> {
                enabledMods.add(m);
                logger.debug("Detected enabled mod " + m.getName());
            }, () -> {
                logger.warn("Detected enabled but unrecognized mod " + s);
            });
        });
    }

    public void startDirectly(boolean debug) throws IOException {
        var args = new ArrayList<>(type.getLaunchArguments());
        if (debug) {
            args.add(type.debugModeSwitch().get());
        }
        dist.startDirectly(type.getExecutable(getInstallDir()), args, Map.of());
    }

    public void loadData() throws InvalidInstallationException {
        Game g = ALL.inverseBidiMap().get(this);
        LoggerFactory.getLogger(getClass()).debug("Initializing " + g.getAbbreviation() + " installation ...");

        if (getInstallDir().startsWith(OsHelper.getUserDocumentsPath().resolve("Paradox Interactive"))) {
            throw new InvalidInstallationException("INSTALL_DIR_IS_USER_DIR", g.getFullName(), g.getFullName());
        }

        if (!Files.isRegularFile(type.getExecutable(getInstallDir()))) {
            var exec = getInstallDir().relativize(type.getExecutable(getInstallDir()));
            throw new InvalidInstallationException("EXECUTABLE_NOT_FOUND", g.getFullName(), exec.toString(), getInstallDir().toString());
        }

        logger.debug(g.getAbbreviation() + " distribution type: " + this.dist.getName());

        try {
            this.userDir = dist.determineUserDir();
            logger.debug(g.getAbbreviation() + " user dir: " + this.userDir);
            if (!Files.exists(this.userDir)) {
                throw new InvalidInstallationException(
                        "GAME_DATA_PATH_DOES_NOT_EXIST", g.getAbbreviation(), this.userDir.toString());
            }

            this.version = dist.determineVersion().map(type::getVersion)
                    .orElse(type.determineVersionFromInstallation(getInstallDir()))
                    .orElse(null);
            logger.debug(g.getAbbreviation() + " version: " + (this.version != null ? this.version : "unknown"));
            this.language = type.determineLanguage(getInstallDir(), userDir).orElse(null);
            logger.debug(g.getAbbreviation() + " language: " +
                    (this.language != null ? this.language.getDisplayName() : "unknown"));
            LoggerFactory.getLogger(getClass()).debug("Finished initialization");
        } catch (InvalidInstallationException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidInstallationException(e);
        }
    }

    public Path getInstallDir() {
        return dist.getInstallLocation();
    }

    public Path getUserDir() {
        return userDir;
    }

    public Path getSavegamesDir() {
        return getUserDir().resolve("save games");
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

    public GameInstallType getType() {
        return type;
    }

    public List<GameMod> queryEnabledMods() {
        try {
            loadEnabledMods();
        } catch (Exception e) {
            ErrorHandler.handleException(e);
        }
        return enabledMods;
    }
}
