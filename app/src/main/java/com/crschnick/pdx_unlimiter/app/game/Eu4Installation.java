package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.savegame.Eu4Campaign;
import com.crschnick.pdx_unlimiter.eu4.parser.GameTag;
import com.crschnick.pdx_unlimiter.eu4.parser.GameVersion;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Eu4Installation extends GameInstallation {

    private Path userDirectory;
    private GameVersion version;
    private Map<String, String> countryNames = new HashMap<>();


    public Eu4Installation(Path path) {
        super("Europa Universalis IV", path);
    }

    public void init() throws Exception {
        for (File f : getPath().resolve("history").resolve("countries").toFile().listFiles()) {
            String[] s = f.getName().split("-");
            countryNames.put(s[0].trim(), s[1].substring(0, s[1].length() - 4).trim());
        }

        loadSettings();
    }

    public void loadSettings() throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(getPath().resolve("launcher-settings.json")));
        this.userDirectory = Paths.get(node.get("gameDataPath").textValue()
                .replace("%USER_DOCUMENTS%", Paths.get(System.getProperty("user.home"), "Documents").toString()));
        String v = node.get("version").textValue();
        Matcher m = Pattern.compile("v(\\d)\\.(\\d+)\\.(\\d+)\\.(\\d+)").matcher(v);
        m.find();
        this.version = new GameVersion(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4)));
    }

    public void writeLaunchConfig(Eu4Campaign.Entry entry, Path path) throws IOException {
        var out = Files.newOutputStream(getUserDirectory().resolve("continue_game.json"));
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonGenerator generator = factory.createGenerator(out);
        generator.setPrettyPrinter(new DefaultPrettyPrinter());
        ObjectNode n = mapper.createObjectNode()
                .put("title", entry.getCampaign().getName())
                .put("desc", entry.getName())
                .put("date", entry.getCampaign().getLastPlayed().toString())
                .put("filename", path.toString().replace('\\', '/'));
        mapper.writeTree(generator, n);
        out.close();
    }

    @Override
    public void start() {
        try {
            Runtime.getRuntime().exec(getPath().resolve("eu4.exe").toString() + " -continuelastsave");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPreexistingCoutry(String tag) {
        return countryNames.containsKey(tag);
    }

    public String getCountryName(GameTag tag) {
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
        return getPath().resolve("eu4.exe").toFile().exists();
    }

    public Path getUserDirectory() {
        return userDirectory;
    }

    public Path getSaveDirectory() {
        return userDirectory.resolve("save games");
    }

    public GameVersion getVersion() {
        return version;
    }
}
