package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ck3Installation extends GameInstallation {

    public Ck3Installation(Path path) {
        super(path, Path.of("binaries", "ck3"));
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
        var out = Files.newOutputStream(getUserPath().resolve("continue_game.json"));
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        var date = d.format(new Date(lastPlayed.toEpochMilli()));
        var sgPath = FilenameUtils.getBaseName(
                FilenameUtils.separatorsToUnix(getSavegamesPath().relativize(path).toString()));
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", sgPath)
                .put("desc", "")
                .put("date", date);
        JsonHelper.write(n, out);
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
        this.userDir = determineUserDirectory(node);
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
    public void startDirectly() throws IOException {
        new ProcessBuilder().command(getExecutable().toString(), "--continuelastsave").start();
    }
}
