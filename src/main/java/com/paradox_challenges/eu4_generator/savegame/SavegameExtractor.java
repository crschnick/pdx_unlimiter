package com.paradox_challenges.eu4_generator.savegame;

import com.paradox_challenges.eu4_generator.format.Namespace;
import com.paradox_challenges.eu4_generator.format.NodeSplitter;
import com.paradox_challenges.eu4_generator.format.NodeTransformer;
import com.paradox_challenges.eu4_generator.format.eu4.Eu4Transformer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SavegameExtractor {

    private GamestateParser parser;

    private NodeTransformer gamestateTransformer;

    private NodeSplitter splitter;

    public SavegameExtractor() {
        parser = new GamestateParser(Namespace.EU4);
        gamestateTransformer = new Eu4Transformer();
        splitter = new NodeSplitter("wars", "provinces", "military", "religion", "countries");
    }

    private static ZipEntry getEntryByName(String name, ZipFile zipFile) {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(name)) {
                return entry;
            }
        }
        return null;
    }

    private void writeNode(String dir, String name, Node node) throws IOException {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        Path p = Paths.get(dir, name + ".json");
        FileOutputStream out = new FileOutputStream(p.toString());
        out.write(node.toString(0).getBytes());
    }

    public void extract(String inputFile, String outputDir) throws IOException {
        ZipFile zipFile = new ZipFile(inputFile);

        ZipEntry gamestate = getEntryByName("gamestate", zipFile);
        Node gamestateNode = parser.parse(zipFile.getInputStream(gamestate));
        gamestateTransformer.transformNode(gamestateNode);
        Map<String, Node> map = splitter.removeNodes(gamestateNode);
        for (String key : map.keySet()) {
            writeNode(outputDir, key, map.get(key));
        }
        writeNode(outputDir, "gamestate", gamestateNode);

        ZipEntry meta = getEntryByName("meta", zipFile);
        Node metaNode = parser.parse(zipFile.getInputStream(meta));
    }
}
