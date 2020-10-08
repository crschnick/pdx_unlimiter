package com.crschnick.pdx_unlimiter.eu4.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.crschnick.pdx_unlimiter.eu4.io.JsonConverter;
import com.crschnick.pdx_unlimiter.eu4.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.eu4.format.eu4.Eu4Transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Eu4IntermediateSavegame {

    private static final String[] PARTS = new String[] {"gamestate", "meta", "ai", "ongoing_wars", "ended_wars", "provinces", "countries", "trade_nodes", "rebel_factions", "active_advisors", "map_area_data", "religions", "diplomacy", "inflation_statistics", "religion_data"};

    private static final String[] GAMESTATE_SPLIT_PARTS = new String[] {"ongoing_wars", "ended_wars", "provinces", "countries", "trade_nodes", "rebel_factions", "active_advisors", "map_area_data", "religions", "diplomacy", "inflation_statistics", "religion_data"};

    private Map<String, Node> nodes;

    private Eu4IntermediateSavegame(Map<String, Node> nodes) {
        this.nodes = nodes;
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public static Eu4IntermediateSavegame fromSavegame(Eu4Savegame save) {
        Node gameState = save.getGamestate();
        Eu4Transformer.GAMESTATE_TRANSFORMER.transform(gameState);
        Eu4Transformer.META_TRANSFORMER.transform(save.getMeta());
        Map<String, Node> map = new NodeSplitter(GAMESTATE_SPLIT_PARTS).removeNodes(gameState);
        map.put("gamestate", gameState);
        map.put("ai", save.getAi());
        map.put("meta", save.getMeta());
        return new Eu4IntermediateSavegame(map);
    }

    public static Eu4IntermediateSavegame fromFile(Path file) throws IOException {
        return fromFile(file, GAMESTATE_SPLIT_PARTS);
    }
    public static Eu4IntermediateSavegame fromFile(Path file, String... parts) throws IOException {
        Map<String, Node> nodes = new HashMap<>();
        boolean isZipped = new ZipInputStream(Files.newInputStream(file)).getNextEntry() != null;
        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            String txtTest = parts[0] + ".txt";
            boolean txt = false;
            if (zipFile.getEntry(txtTest) != null) {
                txt = true;
            }

            ObjectMapper mapper = new ObjectMapper();
            for (String s : parts) {
                ZipEntry e = zipFile.getEntry(s + (txt ? ".txt" : ".json"));
                if (!txt) {
                    nodes.put(s, JsonConverter.fromJson(mapper.readTree(zipFile.getInputStream(e))));
                } else {

                }
            }
            return new Eu4IntermediateSavegame(nodes);
        } else {
            return null;
        }
    }

    public void write(String fileName, boolean json) throws IOException {
        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        for (String s : nodes.keySet()) {
            ZipEntry e = new ZipEntry(s + (json ? ".json" : ".txt"));
            out.putNextEntry(e);

            if (json) {
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                JsonGenerator generator = factory.createGenerator(out);
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
                ObjectNode n = mapper.createObjectNode();
                JsonConverter.toJsonObject(n, nodes.get(s));
                mapper.writeTree(generator, n);
            } else {
                byte[] data = nodes.get(s).toString(0).getBytes();
                out.write(data, 0, data.length);
            }

            out.closeEntry();
        }
        out.close();
    }
}
