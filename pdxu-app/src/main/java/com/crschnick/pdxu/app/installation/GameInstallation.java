package com.crschnick.pdxu.app.installation;


import com.crschnick.pdxu.app.installation.dist.GameDist;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.FileSystemHelper;
import com.crschnick.pdxu.model.GameVersion;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class GameInstallation {

    public static final BidiMap<Game, GameInstallation> ALL = new DualLinkedHashBidiMap<>();

    private final GameInstallType type;
    private final GameDist dist;

    private final List<GameDlc> dlcs = new ArrayList<>();
    private final List<GameMod> mods = new ArrayList<>();

    private Path userDir;
    private GameVersion version;
    private GameLanguage language;

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
        TrackEvent.debug("Initializing optional data ...");
        loadDlcs();
        this.mods.addAll(type.loadMods(this));
        TrackEvent.debug("Finished initializing optional data\n");
    }

    private void loadDlcs() throws IOException {
        dlcs.addAll(type.loadDlcs(getInstallDir()));
    }

    public GameDist getDist() {
        return dist;
    }

    public Optional<GameMod> getModForLauncherId(String fn) {
        return getMods().stream().filter(m -> type.getModLauncherId(this, m).equals(fn)).findAny();
    }

    public Optional<GameMod> getModForSavegameId(String id) {
        return getMods().stream().filter(m -> type.getModSavegameId(this, m).equals(id)).findAny();
    }

    public Optional<GameDlc> getDlcForLauncherId(String fn) {
        return getDlcs().stream().filter(m -> type.getDlcLauncherId(this, m).equals(fn)).findAny();
    }

    public Optional<GameDlc> getDlcForSavegameId(String id) {
        return getDlcs().stream().filter(m -> type.getDlcSavegameId(this, m).equals(id)).findAny();
    }

    public void startDirectly(boolean debug) throws IOException {
        var args = new ArrayList<>(type.getLaunchArguments());
        if (debug) {
            args.add(type.debugModeSwitch().get());
        }
        dist.startDirectly(dist.getExecutable(), args, Map.of());
    }

    public void loadData() throws InvalidInstallationException {
        Game g = dist.getGame();
        TrackEvent.debug("Initializing " + g.getTranslatedAbbreviation() + " installation ...");

        if (getInstallDir().startsWith(FileSystemHelper.getUserDocumentsPath().resolve("Paradox Interactive"))) {
            throw new InvalidInstallationException("installDirIsUserDir", g.getInstallationName(), g.getInstallationName());
        }

        if (!Files.isRegularFile(dist.getExecutable())) {
            var exec = getInstallDir().relativize(dist.getExecutable());
            throw new InvalidInstallationException("executableNotFound", g.getInstallationName(), exec.toString(), getInstallDir().toString());
        }

        TrackEvent.debug(g.getTranslatedAbbreviation() + " distribution type: " + this.dist.getName());

        try {
            this.userDir = dist.determineUserDir();
            TrackEvent.debug(g.getTranslatedAbbreviation() + " user dir: " + this.userDir);
            if (!Files.exists(this.userDir)) {
                throw new InvalidInstallationException(
                        "gameDataPathDoesNotExist", g.getTranslatedAbbreviation(), this.userDir.toString());
            }

            this.version = dist.determineVersion().map(type::getVersion)
                    .orElse(type.determineVersionFromInstallation(getInstallDir()))
                    .orElse(null);
            TrackEvent.debug(g.getTranslatedAbbreviation() + " version: " + (this.version != null ? this.version : "unknown"));
            this.language = type.determineLanguage(getInstallDir(), userDir).orElse(null);
            TrackEvent.debug(g.getTranslatedAbbreviation() + " language: " +
                    (this.language != null ? this.language.getId() : "unknown"));
            TrackEvent.debug("Finished initialization");
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

    public GameLanguage getLanguage() {
        return language;
    }

    public GameInstallType getType() {
        return type;
    }

    public List<GameMod> queryEnabledMods() throws Exception {
        TrackEvent.debug("Loading enabled mods ...");
        var enabledMods = new ArrayList<GameMod>();
        type.getEnabledMods(getInstallDir(), userDir).forEach(s -> {
            var mod = getModForLauncherId(s);
            mod.ifPresentOrElse(m -> {
                enabledMods.add(m);
                TrackEvent.debug("Detected enabled mod " + m.getName());
            }, () -> {
                TrackEvent.warn("Detected enabled but unrecognized mod " + s);
            });
        });
        return enabledMods;
    }

    public List<GameDlc> queryDisabledDlcs() throws Exception {
        TrackEvent.debug("Loading disabled dlcs ...");
        var disabledDlcs = new ArrayList<GameDlc>();
        type.getDisabledDlcs(getInstallDir(), userDir).forEach(s -> {
            var dlc = getDlcForLauncherId(s);
            dlc.ifPresentOrElse(m -> {
                disabledDlcs.add(m);
                TrackEvent.debug("Detected disabled dlc " + m.getName());
            }, () -> {
                TrackEvent.warn("Detected disabled but unrecognized dlc " + s);
            });
        });
        return disabledDlcs;
    }
}
