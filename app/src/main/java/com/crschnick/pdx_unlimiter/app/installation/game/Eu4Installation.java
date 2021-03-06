package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.DistributionType;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Eu4Installation extends GameInstallation {

    public Eu4Installation(Path path) {
        super(path, Path.of("eu4"));
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {
        var sgPath = FilenameUtils.separatorsToUnix(getUserPath().relativize(path).toString());
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", name)
                .put("desc", "")
                .put("date", lastPlayed.toString())
                .put("filename", sgPath);
        JsonHelper.write(n, getUserPath().resolve("continue_game.json"));
    }

    public void loadData() throws Exception {
        loadSettings();
    }

    private Path determineUserDirectory(JsonNode node) {
        var userdirFile = getPath().resolve("userdir.txt");
        if (Files.exists(userdirFile)) {
            try {
                String userdir = Files.readString(userdirFile);
                if (!userdir.isEmpty()) {
                    logger.debug("Found custom userdir " + userdir);
                    return Path.of(userdir);
                }
            } catch (IOException e) {
                ErrorHandler.handleException(e);
            }
        }

        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in EU4 launcher config file"))
                .textValue();
        return super.replaceVariablesInPath(value);
    }

    public void loadSettings() throws Exception {
        if (!Files.isRegularFile(getExecutable())) {
            throw new IllegalArgumentException("EU4 executable " + getExecutable() + " not found");
        }

        var ls = getPath().resolve("launcher-settings.json");
        if (!Files.exists(ls)) {
            throw new IOException("Missing EU4 Paradox launcher settings. " +
                    "Only EU4 installations using the Paradox Launcher are supported");
        }

        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(ls));
        super.userDir = determineUserDirectory(node);
        if (!Files.exists(userDir)) {
            throw new IllegalArgumentException("EU4 user directory " + userDir +
                    " does not exist. To fix this problem, please run EU4 once to generate it");
        }

        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)\\.\\w+\\s.+").matcher(v);
        m.find();
        this.version = new GameVersion(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                m.group(5));
        logger.debug("EU4 version: " + v);
        String platform = node.required("distPlatform").textValue();
        logger.debug("Distribution platform: " + platform);
        if (platform.equals("steam")) {
            // Trim the id because sometimes it contains trailing new lines!
            var id = Files.readString(getPath().resolve("steam_appid.txt")).trim();
            this.distType = new DistributionType.Steam(Integer.parseInt(id));
        } else {
            this.distType = new DistributionType.PdxLauncher(getLauncherDataPath());
        }
    }

    @Override
    public void startDirectly() {
        try {
            new ProcessBuilder().command(getExecutable().toString(), "-continuelastsave").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return mods.stream().filter(d -> getUserPath().relativize(d.getModFile()).equals(Path.of(name))).findAny();
    }
}
