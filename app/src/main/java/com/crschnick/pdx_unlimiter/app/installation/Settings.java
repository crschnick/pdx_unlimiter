package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.game.Eu4Installation;
import com.crschnick.pdx_unlimiter.app.game.GameInstallation;
import com.crschnick.pdx_unlimiter.app.game.Hoi4Installation;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static Settings INSTANCE;
    private Path eu4;
    private Path hoi4;

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

    public static Settings getInstance() {
        return INSTANCE;
    }

    public static void updateSettings(Settings newS) {
        INSTANCE = newS;
        INSTANCE.validate();
        INSTANCE.apply();
    }

    private static Settings defaultSettings() {
        Settings s = new Settings();
        s.eu4 = GameInstallation.getInstallPath("Europa Universalis IV").orElse(null);
        s.hoi4 = GameInstallation.getInstallPath("Hearts of Iron IV").orElse(null);
        return s;
    }

    private static Settings loadConfig(Path file) throws Exception {
        JsonNode node = new ObjectMapper().readTree(Files.readAllBytes(file));
        JsonNode i = node.required("installations");
        Settings s = new Settings();
        s.eu4 = Optional.ofNullable(i.get("eu4")).map(n -> Paths.get(n.textValue())).orElse(null);
        s.hoi4 = Optional.ofNullable(i.get("hoi4")).map(n -> Paths.get(n.textValue())).orElse(null);
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
        if (s.eu4 != null) {
            i.set("eu4", new TextNode(s.eu4.toString()));
        }
        if (s.hoi4 != null) {
            i.set("hoi4", new TextNode(s.hoi4.toString()));
        }

        mapper.writeTree(generator, n);
        out.close();
    }

    public Settings copy() {
        Settings c = new Settings();
        c.eu4 = eu4;
        c.hoi4 = hoi4;
        return c;
    }

    public Optional<Path> getEu4() {
        return Optional.ofNullable(eu4);
    }

    public void setEu4(Path eu4) {
        this.eu4 = eu4;
    }

    public void validate() {
        if (eu4 != null && !new Eu4Installation(eu4).isValid()) {
            eu4 = null;
        }
        if (hoi4 != null && !new Hoi4Installation(hoi4).isValid()) {
            hoi4 = null;
        }
    }

    public void apply() {
        if (eu4 != null) {
            GameInstallation.EU4 = new Eu4Installation(eu4);
        }
        if (hoi4 != null) {
            GameInstallation.HOI4 = new Eu4Installation(hoi4);
        }
    }
}
