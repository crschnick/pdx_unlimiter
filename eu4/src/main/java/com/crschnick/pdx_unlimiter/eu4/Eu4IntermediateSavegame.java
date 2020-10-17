package com.crschnick.pdx_unlimiter.eu4;

import com.crschnick.pdx_unlimiter.eu4.parser.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.crschnick.pdx_unlimiter.eu4.io.JsonConverter;
import com.crschnick.pdx_unlimiter.eu4.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.eu4.format.eu4.Eu4Transformer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Eu4IntermediateSavegame {

    public static final int VERSION = 2;

    private static final String[] GAMESTATE_SPLIT_PARTS = new String[] {"active_wars", "previous_wars", "provinces", "countries", "countries_history", "trade_nodes", "rebel_factions", "active_advisors", "map_area_data", "religions", "diplomacy", "inflation_statistics", "religion_data"};

    private int version;

    private Map<String, Node> nodes;

    private Eu4IntermediateSavegame(Map<String, Node> nodes, int version) {
        this.nodes = nodes;
        this.version = version;
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public static Eu4IntermediateSavegame fromSavegame(Eu4Savegame save) throws SavegameParseException {
        Node gameState = save.getGamestate();
        Map<String, Node> map;
        try {
            Eu4Transformer.GAMESTATE_TRANSFORMER.transform(gameState);
            Eu4Transformer.META_TRANSFORMER.transform(save.getMeta());
            map = new NodeSplitter(GAMESTATE_SPLIT_PARTS).removeNodes(gameState);
        } catch (RuntimeException e) {
            throw new SavegameParseException("Can't transform savegame", e);
        }
        map.put("gamestate", gameState);
        map.put("ai", save.getAi());
        map.put("meta", save.getMeta());
        return new Eu4IntermediateSavegame(map, VERSION);
    }

    public static int getVersion(Path file) throws IOException {
        int v = 0;
        boolean isDir = file.toFile().isDirectory();
        if (!isDir) {
            ZipFile zipFile = new ZipFile(file.toFile());
            v = Integer.parseInt(new String(zipFile.getInputStream(zipFile.getEntry("version")).readAllBytes()));
        } else {
            v = Integer.parseInt(new String(Files.readAllBytes(file.resolve("version"))));
        }
        return v;
    }

    public static Eu4IntermediateSavegame fromFile(Path file) throws IOException {
        return fromFile(file, GAMESTATE_SPLIT_PARTS);
    }

    public static Eu4IntermediateSavegame fromFile(Path file, String... parts) throws IOException {
        Map<String, Node> nodes = new HashMap<>();
        boolean isDir = file.toFile().isDirectory();
        int v = 0;
        if (!isDir) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ObjectMapper mapper = new ObjectMapper();
            for (String s : parts) {
                ZipEntry e = zipFile.getEntry(s + ".json");
                nodes.put(s, JsonConverter.fromJson(mapper.readTree(zipFile.getInputStream(e))));
            }
            v = Integer.parseInt(new String(zipFile.getInputStream(zipFile.getEntry("version")).readAllBytes()));
        } else {
            ObjectMapper mapper = new ObjectMapper();
            for (String s : parts) {
                nodes.put(s, JsonConverter.fromJson(mapper.readTree(Files.newInputStream(file.resolve(s + ".json")))));
            }
            v = Integer.parseInt(new String(Files.readAllBytes(file.resolve("version"))));
        }
        if (v != VERSION) {
            throw new IOException("Incompatible savegame version " + v + ", required: " + VERSION);
        }
        return new Eu4IntermediateSavegame(nodes, v);
    }

    private void writeEntry(OutputStream out, Node node) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonGenerator generator = factory.createGenerator(out);
        generator.setPrettyPrinter(new DefaultPrettyPrinter());
        new ObjectMapper().writeTree(generator, JsonConverter.toJsonObject(node));
    }

    public void write(Path path, boolean zip) throws IOException {
        if (zip) {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path.toFile()));
            for (String s : nodes.keySet()) {
                ZipEntry e = new ZipEntry(s + ".json");
                out.putNextEntry(e);
                writeEntry(out, nodes.get(s));
                out.closeEntry();
            }
            out.putNextEntry(new ZipEntry("version"));
            out.write(String.valueOf(version).getBytes());
            out.closeEntry();

            out.close();
        } else {
            path.toFile().mkdir();
            for (String s : nodes.keySet()) {
                OutputStream out = Files.newOutputStream(path.resolve(s + ".json"));
                writeEntry(out, nodes.get(s));
                out.close();
            }

            Files.write(path.resolve("version"), String.valueOf(version).getBytes());
        }
    }

    public int getVersion() {
        return version;
    }
}
