package com.paradox_challenges.eu4_unlimiter.parser.eu4;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paradox_challenges.eu4_unlimiter.io.JsonConverter;
import com.paradox_challenges.eu4_unlimiter.format.NodeSplitter;
import com.paradox_challenges.eu4_unlimiter.format.eu4.Eu4Transformer;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

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

    private static final String[] GAMESTATE_PARTS = new String[] {"ongoing_wars", "ended_wars", "provinces", "countries", "trade_nodes", "rebel_factions", "active_advisors", "map_area_data", "religions", "diplomacy", "inflation_statistics", "religion_data"};

    private Map<String, Node> nodes;

    private Eu4IntermediateSavegame(Map<String, Node> nodes) {
        this.nodes = nodes;
    }

    public static Eu4IntermediateSavegame fromSavegame(Eu4Savegame save) {
        Node gameState = save.getGamestate();
        Eu4Transformer.GAMESTATE_TRANSFORMER.transform(gameState);
        Map<String, Node> map = new NodeSplitter(GAMESTATE_PARTS).removeNodes(gameState);
        map.put("gamestate", gameState);
        map.put("ai", save.getAi());
        map.put("meta", save.getMeta());
        return new Eu4IntermediateSavegame(map);
    }

    public static Eu4IntermediateSavegame fromFile(Path file) throws IOException {
        Map<String, Node> nodes = new HashMap<>();
        boolean isZipped = new ZipInputStream(Files.newInputStream(file)).getNextEntry() != null;
        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            String txtTest = GAMESTATE_PARTS[0] + ".txt";
            boolean txt = false;
            if (zipFile.getEntry(txtTest) != null) {
                txt = true;
            }

            ObjectMapper mapper = new ObjectMapper();
            for (String s : GAMESTATE_PARTS) {
                ZipEntry e = zipFile.getEntry(s + (txt ? ".txt" : ".json"));
                if (!txt) {
                    nodes.put(s, JsonConverter.fromJson(mapper.readTree(zipFile.getInputStream(e))));
                } else {

                }
            }
            ZipEntry ai = zipFile.getEntry("ai" + (txt ? ".txt" : ".json"));
            nodes.put("ai", JsonConverter.fromJson(mapper.readTree(zipFile.getInputStream(ai))));
            ZipEntry meta = zipFile.getEntry("meta" + (txt ? ".txt" : ".json"));
            nodes.put("meta", JsonConverter.fromJson(mapper.readTree(zipFile.getInputStream(meta))));
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
