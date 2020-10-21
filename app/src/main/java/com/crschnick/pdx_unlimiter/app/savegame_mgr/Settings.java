package com.crschnick.pdx_unlimiter.app.savegame_mgr;

import com.crschnick.pdx_unlimiter.app.installation.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.app.installation.PdxuInstallation;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Settings {

    public static void init() throws Exception {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("installations.json");
        if (!file.toFile().exists()) {
            INSTANCE = defaultSettings();
        } else {
            INSTANCE = loadConfig(file);
        }
        INSTANCE.validate();
        INSTANCE.apply();
    }

    private static Settings INSTANCE;

    public static Settings getInstance() {
        return INSTANCE;
    }

    public void setEu4(Optional<Path> eu4) {
        this.eu4 = eu4;
    }

    public Settings copy() {
        Settings c = new Settings();
        c.eu4 = eu4;
        return c;
    }

    public Optional<Path> getEu4() {
        return eu4;
    }

    public static void updateSettings(Settings newS) {
        INSTANCE = newS;
        INSTANCE.validate();
        INSTANCE.apply();
    }

    private static Settings defaultSettings() {
        Settings s = new Settings();
        s.eu4 = GameInstallation.getInstallPath("Europa Universalis IV");
        return s;
    }

    private static Settings loadConfig(Path file) throws Exception {
        JsonNode node = new ObjectMapper().readTree(Files.readAllBytes(file));
        JsonNode i = node.get("installations");
        Settings s = new Settings();
        s.eu4 = Optional.ofNullable(i.get("eu4")).map(n -> Paths.get(n.textValue()));
        return s;
    }

    public static void saveConfig() throws IOException {
        Path file = PdxuInstallation.getInstance().getSettingsLocation().resolve("installations.json");
        FileUtils.forceMkdirParent(file.toFile());
        var out = Files.newOutputStream(file);

        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator generator = factory.createGenerator(out);
        generator.setPrettyPrinter(new DefaultPrettyPrinter());
        ObjectNode n = JsonNodeFactory.instance.objectNode();
        ObjectNode i = n.putObject("installations");

        Settings s = Settings.INSTANCE;
        s.eu4.ifPresent(path -> i.set("eu4", new TextNode(path.toString())));

        mapper.writeTree(generator, n);
        out.close();
    }

    private Optional<Path> eu4;

    public void validate() {
        if (eu4.isPresent() && !new Eu4Installation(eu4.get()).isValid()) {
            eu4 = Optional.empty();
        }
    }

    public void apply() {
        eu4.ifPresentOrElse(
                value -> GameInstallation.EU4 = new Eu4Installation(value),
                () -> GameInstallation.EU4 = null);
    }
}
