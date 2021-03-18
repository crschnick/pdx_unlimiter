package com.crschnick.pdx_unlimiter.app.installation.game;

import com.crschnick.pdx_unlimiter.app.installation.Game;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.GameMod;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.app.util.LocalisationHelper;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StellarisInstallation extends GameInstallation {

    public StellarisInstallation(Path path) {
        super(path, Path.of("stellaris"));
    }

    public <T, I extends SavegameInfo<T>> Path getExportTarget(SavegameEntry<T, I> e) {
        Path file;
        Path dir = getSavegamesPath().resolve(SavegameStorage.<T, I>get(Game.STELLARIS).getEntryName(e));
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
    public void startDirectly() throws IOException {
        new ProcessBuilder().command(getExecutable().toString(), "-gdpr-compliant", "--continuelastsave").start();
    }

    @Override
    protected GameVersion determineVersion(JsonNode node) {
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("(\\w)+\\s+v(\\d)\\.(\\d+)\\.(\\d+).+").matcher(v);
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
}
