package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.parser.GameTag;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hoi4Installation extends GameInstallation {

    private Path executable;
    private Path userDirectory;


    public Hoi4Installation(Path path) {
        super(path);
        if (SystemUtils.IS_OS_WINDOWS) {
            executable = getPath().resolve("hoi4.exe");
        }
        else if (SystemUtils.IS_OS_LINUX) {
            executable = getPath().resolve("hoi");
        }
    }

    public Path getExecutable() {
        return executable;
    }

    public void init() throws Exception {
        loadSettings();
    }

    private Path determineUserDirectory(JsonNode node) {
        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in HOI4 launcher config file"))
                .textValue();
        return replaceVariablesInPath(value);
    }

    public void loadSettings() throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getPath().resolve("launcher-settings.json")));
        this.userDirectory = determineUserDirectory(node);
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)").matcher(v);
        m.find();
    }

    @Override
    public void start() {
        try {
            new ProcessBuilder().command(executable.toString()).start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    @Override
    public boolean isValid() {
        return Files.isRegularFile(executable);
    }

    public Path getUserDirectory() {
        return userDirectory;
    }

    public Path getSaveDirectory() {
        return userDirectory.resolve("save games");
    }
}
