package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.data.GameVersion;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;
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

public class StellarisInstallation extends GameInstallation {

    public StellarisInstallation(Path path) {
        super("stellaris", path, Path.of("stellaris"));
    }

    public <T, I extends SavegameInfo<T>> Path getExportTarget(
            SavegameCache<?, ?, T, I> cache, GameCampaignEntry<T, I> e) {
        Path file;
        Path dir = getSavegamesPath().resolve(cache.getEntryName(e));
        if (e.getInfo().isIronman()) {
            file = dir.resolve("ironman.sav");
        } else {
            file = dir.resolve(e.getDate().toString() + ".sav");
        }
        return file;
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {
        var out = Files.newOutputStream(getUserPath().resolve("continue_game.json"));
        var sgPath = FilenameUtils.getBaseName(
                FilenameUtils.separatorsToUnix(getUserPath().relativize(path).toString()));
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", sgPath)
                .put("desc", name)
                .put("date", "");
        JsonHelper.write(n, out);
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return Optional.empty();
    }

    @Override
    public void startDirectly() throws IOException {
        new ProcessBuilder().command(getExecutable().toString(), "--continuelastsave").start();
    }

    @Override
    public void loadData() throws Exception {
        if (!Files.isRegularFile(getExecutable())) {
            throw new IllegalArgumentException("Executable not found");
        }

        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getPath().resolve("launcher-settings.json")));
        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in Stellaris launcher config file"))
                .textValue();
        this.userDir = super.replaceVariablesInPath(value);
        if (!Files.exists(userDir)) {
            throw new IllegalArgumentException("User directory not found");
        }

        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\w)+\\s+v(\\d)\\.(\\d+)\\.(\\d+).+").matcher(v);
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
}
