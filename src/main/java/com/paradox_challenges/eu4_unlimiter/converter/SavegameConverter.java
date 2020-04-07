package com.paradox_challenges.eu4_unlimiter.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paradox_challenges.eu4_unlimiter.format.NodeSplitter;
import com.paradox_challenges.eu4_unlimiter.format.NodeTransformer;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SavegameConverter {

    private String gamestateName;

    private NodeTransformer transformer;

    private String[] names;

    public SavegameConverter(NodeTransformer transformer, String gamestateName, String... names) {
        this.transformer = transformer;
        this.gamestateName = gamestateName;
        this.names = names;
    }

    public void extract(Node node, String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        transformer.transform(node);
        Map<String, Node> map = new NodeSplitter(names).removeNodes(node);

        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        for (String s : names) {
            ZipEntry e = new ZipEntry(s + ".json");
            out.putNextEntry(e);
            byte[] data = mapper.writeValueAsBytes(JsonConverter.toJsonObject(map.get(s)));
            out.write(data, 0, data.length);
            out.closeEntry();
        }

        ZipEntry e = new ZipEntry(gamestateName + ".json");
        out.putNextEntry(e);
        byte[] data = mapper.writeValueAsBytes(JsonConverter.toJsonObject(node));
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
    }
}
