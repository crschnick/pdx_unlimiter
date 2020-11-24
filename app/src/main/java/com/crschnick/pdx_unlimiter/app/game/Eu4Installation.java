package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.data.Eu4Tag;
import com.crschnick.pdx_unlimiter.eu4.data.Eu4Version;
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

public class Eu4Installation extends GameInstallation {

    private Path executable;
    private Path userDirectory;
    private Eu4Version version;
    private Map<String, String> countryNames = new HashMap<>();


    public Eu4Installation(Path path) {
        super(path);
        if (SystemUtils.IS_OS_WINDOWS) {
            executable = getPath().resolve("eu4.exe");
        } else if (SystemUtils.IS_OS_LINUX) {
            executable = getPath().resolve("eu4");
        }
    }

    public Path getExecutable() {
        return executable;
    }

    public void init() throws Exception {
        for (File f : getPath().resolve("history").resolve("countries").toFile().listFiles()) {
            String[] s = f.getName().split("-");
            countryNames.put(s[0].trim(), s[1].substring(0, s[1].length() - 4).trim());
        }

        loadSettings();
    }

    private Path determineUserDirectory(JsonNode node) {
        try {
            String userdir = Files.readString(getPath().resolve("userdir.txt"));
            if (!userdir.isEmpty()) {
                return Path.of(userdir);
            }
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }

        String value = Optional.ofNullable(node.get("gameDataPath"))
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find game data path in EU4 launcher config file"))
                .textValue();
        if (SystemUtils.IS_OS_WINDOWS) {
            value = value.replace("%USER_DOCUMENTS%",
                    Paths.get(System.getProperty("user.home"), "Documents").toString());
        } else if (SystemUtils.IS_OS_LINUX) {
            value = value.replace("$LINUX_DATA_HOME",
                    Paths.get(System.getProperty("user.home"), ".local", "share").toString());
        }

        return Path.of(value);
    }

    public void loadSettings() throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getPath().resolve("launcher-settings.json")));
        this.userDirectory = determineUserDirectory(node);
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)").matcher(v);
        m.find();
        this.version = new Eu4Version(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
    }

    @Override
    public void start(boolean continueLast) {
        try {
            new ProcessBuilder().command(executable.toString(), continueLast ? "-continuelastsave" : "").start();
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public boolean isPreexistingCoutry(String tag) {
        return countryNames.containsKey(tag);
    }

    public String getCountryName(Eu4Tag tag) {
        if (tag.isCustom()) {
            return tag.getName();
        }
        if (!countryNames.containsKey(tag.getTag())) {
            throw new IllegalArgumentException("Invalid country tag " + tag.getTag());
        }
        return countryNames.get(tag.getTag());
    }

    @Override
    public boolean isValid() {
        return Files.isRegularFile(executable);
    }

    @Override
    public Path getSavegamesPath() {
        return userDirectory.resolve("save games");
    }

    public Path getUserPath() {
        return userDirectory;
    }

    public Eu4Version getVersion() {
        return version;
    }
}
