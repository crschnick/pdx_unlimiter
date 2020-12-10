package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.installation.Settings;
import com.crschnick.pdx_unlimiter.app.util.JsonHelper;
import com.crschnick.pdx_unlimiter.core.data.Hoi4Tag;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.crschnick.pdx_unlimiter.core.parser.TextFormatParser;
import com.crschnick.pdx_unlimiter.core.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hoi4Installation extends GameInstallation {

    private Map<Hoi4Tag, String> countryNames;
    private Map<String, Integer> countryColors;


    public Hoi4Installation(Path path) {
        super(path, Path.of("hoi4"));
    }

    public void loadData() throws Exception {
        loadSettings();
        countryNames = new HashMap<>();
        countryColors = new HashMap<>();
    }

    @Override
    public void initOptional() throws Exception {
        super.initOptional();

        Pattern p = Pattern.compile("\\s+([A-Za-z]+)_([a-z]+):0 \"(.+)\"");
        Files.lines(getPath().resolve("localisation").resolve("countries_l_english.yml")).forEach(s -> {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                countryNames.put(new Hoi4Tag(m.group(1), m.group(2)), m.group(3));
            }
        });

        loadCountryColors(getPath().resolve("common").resolve("country_tags").resolve("00_countries.txt"));
        loadCountryColors(getPath().resolve("common").resolve("country_tags").resolve("01_countries.txt"));
        loadCountryColors(getPath().resolve("common").resolve("country_tags").resolve("zz_dynamic_countries.txt"));
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

    private void loadCountryColors(Path path) throws IOException {
        Node node = TextFormatParser.textFileParser().parse(
                Files.newInputStream(path)).get();
        for (Node n : Node.getNodeArray(node)) {
            var kv = Node.getKeyValueNode(n);
            if (!(((ValueNode) kv.getNode()).getValue() instanceof String)) {
                continue;
            }

            Node data = TextFormatParser.textFileParser().parse(
                    Files.newInputStream(getPath().resolve("common").resolve(Node.getString(kv.getNode())))).get();
            List<Node> color;

            // Fix rgb prefix for some countries
            if (Node.getNodeForKey(data, "color") instanceof ValueNode) {
                color = Node.getNodeArray(Node.getNodeArray(data).get(3));
            } else {
                color = Node.getNodeArray(Node.getNodeForKey(data, "color"));
            }

            countryColors.put(kv.getKeyName(), Node.getInteger(color.get(0)) << 16 +
                    (Node.getInteger(color.get(1)) << 8) +
                    (Node.getInteger(color.get(2))));
        }
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
        this.userDir = determineUserDirectory(node);
        String v = node.required("version").textValue();
        Matcher m = Pattern.compile("v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)").matcher(v);
        m.find();

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

    public Map<Hoi4Tag, String> getCountryNames() {
        return countryNames;
    }

    public Map<String, Integer> getCountryColors() {
        return countryColors;
    }
}
