package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StellarisInstallation extends GameInstallation {

    private Path executable;
    private Path userDirectory;

    public StellarisInstallation(Path path) {
        super(path);
        if (SystemUtils.IS_OS_WINDOWS) {
            executable = getPath().resolve("stellaris.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            executable = getPath().resolve("stellaris");
        }
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {

    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return Optional.empty();
    }

    @Override
    public void start() {

    }

    @Override
    public void init() throws Exception {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getPath().resolve("launcher-settings.json")));
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
        this.userDirectory = Path.of(value);

        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\w)+\\s+v(\\d)\\.(\\d+)\\.(\\d+).+").matcher(v);
        m.find();
        this.version = new GameVersion(
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                0,
                m.group(1));
    }

    @Override
    public boolean isValid() {
        return Files.isRegularFile(executable);
    }

    @Override
    public Path getExecutable() {
        return executable;
    }

    @Override
    public Path getUserPath() {
        return userDirectory;
    }


    @Override
    public Path getSavegamesPath() {
        return userDirectory.resolve("save games");
    }
}
