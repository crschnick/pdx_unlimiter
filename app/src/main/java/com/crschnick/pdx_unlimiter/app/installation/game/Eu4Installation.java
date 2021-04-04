package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.installation.InvalidInstallationException;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.fasterxml.jackson.databind.JsonNode;
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

    @Override
    protected Path determineUserDir(JsonNode node) throws InvalidInstallationException {
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

        return super.determineUserDir(node);
    }

    @Override
    protected GameVersion determineVersion(JsonNode node) {
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("\\w+\\s+v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)\\s+(\\w+)\\.\\w+\\s.+").matcher(v);
        m.find();
        return new GameVersion(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                m.group(5));
    }

    @Override
    protected LocalisationHelper.Language determineLanguage() throws Exception {
        var sf = getUserPath().resolve("settings.txt");
        if (!Files.exists(sf)) {
            return null;
        }

        var node = TextFormatParser.textFileParser().parse(sf);
        var langId = node.getNodeForKey("language").getString();
        return LocalisationHelper.Language.byId(langId);
    }

    @Override
    public void startDirectly() throws IOException {
        new ProcessBuilder().command(getExecutable().toString(), "-continuelastsave").start();
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        // Check whether it is a mod path, and not a mod name
        if (!name.startsWith("mod")) {
            return Optional.empty();
        }

        return getMods().stream().filter(d -> getUserPath().relativize(d.getModFile()).equals(Path.of(name))).findAny();
    }
}
