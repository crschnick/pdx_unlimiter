package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.core.settings.Settings;
import com.crschnick.pdx_unlimiter.app.installation.DistributionType;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
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
        super(path, Path.of("stellaris"));
    }

    public <T, I extends SavegameInfo<T>> Path getExportTarget(
            SavegameStorage<T, I> cache, SavegameEntry<T, I> e) {
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
        var sgPath = FilenameUtils.getBaseName(
                FilenameUtils.separatorsToUnix(getUserPath().relativize(path).toString()));
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", sgPath)
                .put("desc", name)
                .put("date", "");
        JsonHelper.write(n, getUserPath().resolve("continue_game.json"));
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return Optional.empty();
    }

    @Override
    public void startDirectly() {
        try {
            new ProcessBuilder().command(getExecutable().toString(), "-gdpr-compliant", "--continuelastsave").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public void loadData() throws Exception {
        if (!Files.isRegularFile(getExecutable())) {
            throw new IllegalArgumentException("Stellaris executable " + getExecutable() + " not found");
        }

        var ls = getPath().resolve("launcher-settings.json");
        if (!Files.exists(ls)) {
            throw new IOException("Missing Stellaris Paradox launcher settings. " +
                    "Only Stellaris installations using the Paradox Launcher are supported");
        }

        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(ls));
        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in Stellaris launcher config file"))
                .textValue();
        this.userDir = super.replaceVariablesInPath(value);
        if (!Files.exists(userDir)) {
            throw new IllegalArgumentException("Stellaris user directory " + userDir + " not found");
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
        if (platform.equals("steam")) {
            this.distType = new DistributionType.Steam(Integer.parseInt(Files.readString(getPath().resolve("steam_appid.txt"))));
        } else {
            this.distType = new DistributionType.PdxLauncher(getLauncherDataPath());
        }
    }
}
