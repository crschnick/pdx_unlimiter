package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Eu4Installation extends GameInstallation {

    private Path executable;
    private Path userDirectory;


    public Eu4Installation(Path path) {
        super(path);
        if (SystemUtils.IS_OS_WINDOWS) {
            executable = getPath().resolve("eu4.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            executable = getPath().resolve("eu4");
        }
    }

    public Path getExecutable() {
        return executable;
    }

    public void init() throws Exception {
        loadSettings();
    }

    private Path determineUserDirectory(JsonNode node) {
        try {
            String userdir = Files.readString(getPath().resolve("userdir.txt"));
            if (!userdir.isEmpty()) {
                return Path.of(userdir);
            }
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in EU4 launcher config file"))
                .textValue();
        if (SystemUtils.IS_OS_WINDOWS) {
            value = value.replace("%USER_DOCUMENTS%",
                    Paths.get(System.getProperty("user.home"), "Documents").toString());
        } else if (SystemUtils.IS_OS_LINUX) {
            value = value.replace("$LINUX_DATA_HOME",
                    Paths.get(System.getProperty("user.home"), ".local", "share").toString());
        }

        return Path.of(value);
    }

    public void loadSettings() throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getPath().resolve("launcher-settings.json")));
        this.userDirectory = determineUserDirectory(node);
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)\\.\\w+\\s.+").matcher(v);
        m.find();
        this.version = new GameVersion(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                m.group(5));
    }

    @Override
    public void start() {
        try {
            new ProcessBuilder().command(executable.toString(), "-continuelastsave").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return mods.stream().filter(d -> getUserPath().relativize(d.getModFile()).equals(Path.of(name))).findAny();
    }

    @Override
    public boolean isValid() {
        return Files.isRegularFile(executable);
    }

    @Override
    public Path getSavegamesPath() {
        return userDirectory.resolve("save games");
    }

    public Path getUserPath() {
        return userDirectory;
    }
}
