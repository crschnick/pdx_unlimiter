package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.WatcherHelper;
import com.crschnick.pdx_unlimiter.app.util.WindowsRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

public abstract class GameInstallation {

    public static Eu4Installation EU4 = null;
    public static Hoi4Installation HOI4 = null;
    public static Ck3Installation CK3 = null;
    public static StellarisInstallation STELLARIS = null;

    protected ObjectProperty<List<Path>> savegames = new SimpleObjectProperty<>();
    private Path path;
    private List<GameDlc> dlcs = new ArrayList<>();
    protected List<GameMod> mods = new ArrayList<>();

    public GameInstallation(Path path) {
        this.path = path;
    }

    public static void initInstallations() throws Exception {
        if (EU4 != null) {
            EU4.init();
        }

        if (HOI4 != null) {
            HOI4.init();
        }

        initInstallationsOptional();
    }

    public static void initInstallationsOptional() {
        if (EU4 != null) {
            try {
                EU4.initOptional();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }

        if (HOI4 != null) {
            try {
                HOI4.initOptional();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }
    }

    public void initOptional() throws Exception {
        loadDlcs();
        loadMods();
        savegames.set(getLatestSavegames());
        WatcherHelper.startWatcherInDirectory("Savegame watcher", getSavegamesPath(), p -> {
            savegames.set(getLatestSavegames());
        }, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
    }

    public static Optional<Path> getInstallPath(String app) {
        Optional<String> steamDir = Optional.empty();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (ArchUtils.getProcessor().is64Bit()) {
                steamDir = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Valve\\Steam", "InstallPath");
            } else {
                steamDir = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Valve\\Steam", "InstallPath");
            }
        } else if (SystemUtils.IS_OS_LINUX) {
            String s = Path.of(System.getProperty("user.home"), ".steam", "steam").toString();
            steamDir = Optional.ofNullable(Files.isDirectory(Path.of(s)) ? s : null);
        }

        if (steamDir.isEmpty()) {
            return Optional.empty();
        }
        Path p = Paths.get(steamDir.get(), "steamapps", "common", app);
        return p.toFile().exists() ? Optional.of(p) : Optional.empty();
    }

    private void loadDlcs() throws IOException {
        Files.list(getPath().resolve("dlc")).forEach(f -> {
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
                GameMod.fromFile(f).ifPresent(m -> mods.add(m));
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        });
    }

    private List<Path> getLatestSavegames() {
        try {
            return Files.list(getSavegamesPath()).sorted(Comparator.comparingLong(p -> {
                try {
                    return Long.MAX_VALUE - Files.getLastModifiedTime(p).toMillis();
                } catch (IOException e) {
                    ErrorHandler.handleException(e);
                    return 0;
                }
            })).collect(Collectors.toList());
        } catch (IOException e) {
            ErrorHandler.handleException(e);
            return new ArrayList<>();
        }
    }

    protected Path replaceVariablesInPath(String value) {
        if (SystemUtils.IS_OS_WINDOWS) {
            value = value.replace("%USER_DOCUMENTS%",
                    Paths.get(System.getProperty("user.home"), "Documents").toString());
        } else if (SystemUtils.IS_OS_LINUX) {
            value = value.replace("$LINUX_DATA_HOME",
                    Paths.get(System.getProperty("user.home"), ".local", "share").toString());
        }
        return Path.of(value);
    }

    public Optional<GameDlc> getDlcForName(String name) {
        return dlcs.stream().filter(d -> d.getName().equals(name)).findAny();
    }

    public abstract Optional<GameMod> getModForName(String name);

    public abstract void start(boolean continueLast);

    public abstract void init() throws Exception;

    public abstract boolean isValid();

    public Path getPath() {
        return path;
    }

    public abstract Path getExecutable();

    public abstract Path getUserPath();

    public abstract Path getSavegamesPath();

    public List<Path> getSavegames() {
        return savegames.get();
    }

    public ObjectProperty<List<Path>> savegamesProperty() {
        return savegames;
    }
}
