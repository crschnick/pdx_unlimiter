package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.DistributionType;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ck3Installation extends GameInstallation {

    public Ck3Installation(Path path) {
        super(path, Path.of("binaries", "ck3"));
    }

    @Override
    protected GameVersion determineVersion(JsonNode node) {
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\d)\\.(\\d+)\\.(\\d+)\\s+\\((\\w+)\\)").matcher(v);
        m.find();
        return new GameVersion(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                0,
                m.group(4));
    }

    @Override
    protected LocalisationHelper.Language determineLanguage() throws Exception {
        var sf = getUserPath().resolve("pdx_settings.txt");
        var node = TextFormatParser.textFileParser().parse(sf);
        var langId = node.getNodeForKey("\"System\"")
                .getNodeForKey("\"language\"").getNodeForKey("value").getString();
        return LocalisationHelper.Language.byId(langId);
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        var date = d.format(new Date(lastPlayed.toEpochMilli()));
        var sgPath = FilenameUtils.getBaseName(
                FilenameUtils.separatorsToUnix(getSavegamesPath().relativize(path).toString()));
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", sgPath)
                .put("desc", "")
                .put("date", date);
        JsonHelper.write(n, getUserPath().resolve("continue_game.json"));
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return getMods().stream().filter(d -> getUserPath().relativize(d.getModFile()).equals(Path.of(name))).findAny();
    }

    @Override
    public Path getSteamAppIdFile() {
        return getPath().resolve("binaries").resolve("steam_appid.txt");
    }

    @Override
    public Path getModBasePath() {
        return getPath().resolve("game");
    }

    @Override
    public Path getLauncherDataPath() {
        return getPath().resolve("launcher");
    }

    @Override
    public Path getDlcPath() {
        return getPath().resolve("game").resolve("dlc");
    }

    @Override
    public void startDirectly() {
        try {
            new ProcessBuilder().command(getExecutable().toString(), "-gdpr-compliant", "--continuelastsave").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }
}
