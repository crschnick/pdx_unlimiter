package com.paradox_challenges.eu4_unlimiter.converter;

import com.paradox_challenges.eu4_unlimiter.format.Namespace;
import com.paradox_challenges.eu4_unlimiter.format.NodeSplitter;
import com.paradox_challenges.eu4_unlimiter.format.NodeTransformer;
import com.paradox_challenges.eu4_unlimiter.format.eu4.Eu4Transformer;
import com.paradox_challenges.eu4_unlimiter.parser.GamedataParser;
import com.paradox_challenges.eu4_unlimiter.parser.Node;
import com.paradox_challenges.eu4_unlimiter.parser.eu4.Eu4IronmanParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Eu4Converter extends SavegameConverter {

    private GamedataParser parser;

    private NodeTransformer gamestateTransformer;

    private NodeSplitter splitter;

    public Eu4Converter() {
        super("gamestate", "ongoing_wars", "ended_wars", "provinces", "military", "religion", "countries", "trade_nodes", "rebels", "province_count_over_time", "country_scores_over_time", "yearly_income_over_time", "inflation_over_time", "advisors");
        parser = new Eu4IronmanParser();
        gamestateTransformer = new Eu4Transformer();
        splitter = new NodeSplitter("ongoing_wars", "ended_wars", "provinces", "military", "religion", "countries", "trade_nodes", "rebels", "province_count_over_time", "country_scores_over_time", "yearly_income_over_time", "inflation_over_time", "advisors");
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
        Optional<Node> gamestateNode = parser.parse(zipFile.getInputStream(gamestate));
        gamestateTransformer.transformNode(gamestateNode.get());
        writeToFile(gamestateNode.get(), outputDir + ".eu4i");
//        Map<String, Node> map = splitter.removeNodes(gamestateNode);
//        for (String key : map.keySet()) {
//            writeNode(outputDir, key, map.get(key));
//        }
//        writeNode(outputDir, "gamestate", gamestateNode);
//
//        ZipEntry meta = getEntryByName("meta", zipFile);
//        Node metaNode = parser.parse(zipFile.getInputStream(meta));
    }
}
