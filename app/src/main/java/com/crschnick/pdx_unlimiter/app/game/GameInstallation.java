package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.WatcherHelper;
import com.crschnick.pdx_unlimiter.app.util.WindowsRegistry;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.time.Instant;
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
    protected GameVersion version;

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

        if (STELLARIS != null) {
            STELLARIS.init();
        }

        if (CK3 != null) {
            CK3.init();
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

        if (STELLARIS != null) {
            try {
                STELLARIS.initOptional();
            } catch (Exception e) {
                ErrorHandler.handleException(e);
            }
        }

        if (CK3 != null) {
            try {
                CK3.initOptional();
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

    public void startLauncher() {
        Path bootstrapper = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            bootstrapper = Path.of(System.getenv("LOCALAPPDATA"))
                    .resolve("Programs")
                    .resolve("Paradox Interactive")
                    .resolve("bootstrapper-v2.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
        }

        try {
            new ProcessBuilder()
                    .directory(getPath().toFile())
                    .command(bootstrapper.toString(),
                            "--pdxlGameDir", getLauncherDataPath().toString(),
                            "--gameDir", getLauncherDataPath().toString())
                    .start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public void writeDlcLoadFile(List<GameMod> mods, List<GameDlc> dlcs) throws IOException {
        var out = Files.newOutputStream(getUserPath().resolve("dlc_load.json"));
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        n.putArray("enabled_mods").addAll(mods.stream()
                .map(m -> m.getModFile().toString())
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        n.putArray("disabled_dlcs").addAll(this.dlcs.stream()
                .filter(d -> d.isExpansion() && !dlcs.contains(d))
                .map(d -> d.getInfoFilePath().toString())
                .map(JsonNodeFactory.instance::textNode)
                .collect(Collectors.toList()));
        JsonHelper.write(n, out);
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
                            " at " + m.getModFile().toString()+ ". Content exists: " + Files.exists(m.getPath()));
                });
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

    public Path getDlcPath() {
        return getPath().resolve("dlc");
    }

    public abstract void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException;

    public abstract Optional<GameMod> getModForName(String name);

    public abstract void start();

    public abstract void init() throws Exception;

    public abstract boolean isValid();

    protected Path getLauncherDataPath() {
        return getPath();
    }

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

    public GameVersion getVersion() {
        return version;
    }
}
