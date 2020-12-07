package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.eu4.data.GameVersion;
import com.crschnick.pdx_unlimiter.eu4.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ck3Installation extends GameInstallation {

    private Path executable;
    private Path userDirectory;

    public Ck3Installation(Path path) {
        super(path);
        if (SystemUtils.IS_OS_WINDOWS) {
            executable = getPath().resolve("binaries").resolve("ck3.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            executable = getPath().resolve("binaries").resolve("ck3");
        }
    }

    public Path getExecutable() {
        return executable;
    }

    public void init() throws Exception {
        loadSettings();
    }

    @Override
    public void initOptional() throws Exception {
        super.initOptional();
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {

    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return mods.stream().filter(d -> getUserPath().relativize(d.getModFile()).equals(Path.of(name))).findAny();
    }

    private Path determineUserDirectory(JsonNode node) {
        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in CK3 launcher config file"))
                .textValue();
        return replaceVariablesInPath(value);
    }

    public void loadSettings() throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getLauncherDataPath().resolve("launcher-settings.json")));
        this.userDirectory = determineUserDirectory(node);
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\d)\\.(\\d+)\\.(\\d+)\\s+\\((\\w+)\\)").matcher(v);
        m.find();
        this.version = new GameVersion(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                0,
                m.group(4));

        String platform = node.required("distPlatform").textValue();
        if (platform.equals("steam") && Settings.getInstance().startSteam()) {
            this.distType = new DistributionType.Steam(
                    Integer.parseInt(Files.readString(getPath().resolve("binaries").resolve("steam_appid.txt"))));
        } else {
            this.distType = new DistributionType.PdxLauncher(getLauncherDataPath());
        }
    }

    @Override
    public Path getModBasePath() {
        return getPath().resolve("game");
    }

    @Override
    protected Path getLauncherDataPath() {
        return getPath().resolve("launcher");
    }

    @Override
    public Path getDlcPath() {
        return getPath().resolve("game").resolve("dlc");
    }

    @Override
    public void start() {
        try {
            new ProcessBuilder().command(executable.toString(), "--continuelastsave").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public boolean isValid() {
        return Files.isRegularFile(executable);
    }

    public Path getUserPath() {
        return userDirectory;
    }

    @Override
    public Path getSavegamesPath() {
        return userDirectory.resolve("save games");
    }
}
