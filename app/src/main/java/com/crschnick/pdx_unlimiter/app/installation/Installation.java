package com.crschnick.pdx_unlimiter.app.installation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class Installation {

    public static final Path FILE = Paths.get(System.getProperty("user.home"), "pdx_unlimiter", "settings", "installations.json");

    public static Optional<Eu4Installation> EU4 = Optional.empty();

    public static boolean isConfigured() {
        return FILE.toFile().exists();
    }

    public static void loadConfig() throws Exception {
        if (!FILE.toFile().exists()) {
            initInstallations();
            return;
        }

        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(FILE));
        JsonNode i = node.get("installations");
        if (i.has("eu4")) {
            var install = new Eu4Installation(Paths.get(i.get("eu4").textValue()));
            EU4 = Optional.of(install);
        }

        initInstallations();
    }

    public static void saveConfig() throws IOException {
        FILE.toFile().getParentFile().mkdirs();
        var out = Files.newOutputStream(FILE);
        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonGenerator generator = factory.createGenerator(out);
        generator.setPrettyPrinter(new DefaultPrettyPrinter());
        ObjectNode n = mapper.createObjectNode();
        ObjectNode i = mapper.createObjectNode();
        if (EU4.isPresent()) {
            i.set("eu4", new TextNode(EU4.get().getPath().toString()));
        }
        n.set("installations", i);
        mapper.writeTree(generator, n);
    }

    public static void initInstallations() throws Exception {
        if (!EU4.isPresent() || !EU4.get().isValid()) {
            var eu4 = getInstallPath("Europa Universalis IV");
            eu4.ifPresent(value -> EU4 = Optional.of(new Eu4Installation(value)));
        }

        if (EU4.isPresent()) {
            EU4.get().init();
        }
    }

    public static Optional<Path> getInstallPath(String app) {
        Optional<String> steamDir = Optional.empty();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (ArchUtils.getProcessor().is64Bit()) {
                steamDir = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\Valve\\Steam", "InstallPath");
            } else {
                steamDir = WindowsRegistry.readRegistry("HKEY_LOCAL_MACHINE\\SOFTWARE\\Valve\\Steam", "InstallPath");
            }
        }

        if (!steamDir.isPresent()) {
            return Optional.empty();
        }
        Path p = Paths.get(steamDir.get(), "steamapps", "common", app);
        return p.toFile().exists() ? Optional.of(p) : Optional.empty();
    }

    private String name;

    private Path path;

    public Installation(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public abstract void start();

    public abstract void init() throws Exception;

    public abstract boolean isValid();

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }
}
