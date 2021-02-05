package com.crschnick.pdx_unlimiter.app.install.game;

import com.crschnick.pdx_unlimiter.app.core.Settings;
import com.crschnick.pdx_unlimiter.app.install.DistributionType;
import com.crschnick.pdx_unlimiter.app.install.GameInstallation;
import com.crschnick.pdx_unlimiter.app.install.GameMod;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hoi4Installation extends GameInstallation {

    public Hoi4Installation(Path path) {
        super("hoi4", path, Path.of("hoi4"));
    }

    public void loadData() throws Exception {
        loadSettings();
    }

    @Override
    public void initOptional() throws Exception {
        super.initOptional();
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {
        var out = Files.newOutputStream(getUserPath().resolve("continue_game.json"));
        SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", name)
                .put("desc", "")
                .put("date", d.format(new Date(lastPlayed.toEpochMilli())) + "\n")
                .put("filename", getSavegamesPath().relativize(path).toString())
                .put("is_remote", false);
        JsonHelper.write(n, out);
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return mods.stream().filter(d -> d.getName().equals(name)).findAny();
    }

    private Path determineUserDirectory(JsonNode node) {
        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in HOI4 launcher config file"))
                .textValue();
        return replaceVariablesInPath(value);
    }

    public void loadSettings() throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(
                getPath().resolve("launcher-settings.json")));
        this.userDir = determineUserDirectory(node);

        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\w+)\\s+v(\\d)\\.(\\d+)\\.(\\d+)").matcher(v);
        m.find();
        this.version = new GameVersion(
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                0,
                m.group(1));

        String platform = node.required("distPlatform").textValue();
        if (platform.equals("steam") && Settings.getInstance().startSteam()) {
            this.distType = new DistributionType.Steam(Integer.parseInt(Files.readString(getPath().resolve("steam_appid.txt"))));
        } else {
            this.distType = new DistributionType.PdxLauncher(getLauncherDataPath());
        }
    }

    @Override
    public void startDirectly() throws IOException {
        new ProcessBuilder().command(getExecutable().toString(), "--continuelastsave").start();
    }
}
