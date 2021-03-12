package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.core.ErrorHandler;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hoi4Installation extends GameInstallation {

    public Hoi4Installation(Path path) {
        super(path, Path.of("hoi4"));
    }

    @Override
    protected GameVersion determineVersion(JsonNode node) {
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\w+)\\s+v(\\d)\\.(\\d+)\\.(\\d+)").matcher(v);
        m.find();
        return new GameVersion(
                Integer.parseInt(m.group(2)),
                Integer.parseInt(m.group(3)),
                Integer.parseInt(m.group(4)),
                0,
                m.group(1));
    }

    @Override
    protected LocalisationHelper.Language determineLanguage() throws Exception {
        var sf = getUserPath().resolve("settings.txt");
        var node = TextFormatParser.textFileParser().parse(sf);
        var langId = node.getNodeForKey("language").getString();
        return LocalisationHelper.Language.byId(langId);
    }

    @Override
    public void writeLaunchConfig(String name, Instant lastPlayed, Path path) throws IOException {
        SimpleDateFormat d = new SimpleDateFormat("E MMM dd HH:mm:ss yyyy");
        ObjectNode n = JsonNodeFactory.instance.objectNode()
                .put("title", name)
                .put("desc", "")
                .put("date", d.format(new Date(lastPlayed.toEpochMilli())) + "\n")
                .put("filename", getSavegamesPath().relativize(path).toString())
                .put("is_remote", false);
        JsonHelper.write(n, getUserPath().resolve("continue_game.json"));
    }

    @Override
    public Optional<GameMod> getModForName(String name) {
        return getMods().stream().filter(d -> d.getName().equals(name)).findAny();
    }

    @Override
    public void startDirectly() throws IOException {
        new ProcessBuilder().command(getExecutable().toString(), "-gdpr-compliant", "--continuelastsave").start();
    }
}
