package com.paradox_challenges.eu4_unlimiter.converter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    public void extract(Node node, String fileName, boolean json) throws IOException {
        transformer.transform(node);
        Map<String, Node> map = new NodeSplitter(names).removeNodes(node);
        map.put(gamestateName, node);

        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        for (String s : map.keySet()) {
            ZipEntry e = new ZipEntry(s + (json ? ".json" : ".txt"));
            out.putNextEntry(e);

            if (json) {
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                JsonGenerator generator = factory.createGenerator(out);
                generator.setPrettyPrinter(new DefaultPrettyPrinter());
                ObjectNode n = mapper.createObjectNode();
                JsonConverter.toJsonObject(n, map.get(s));
                mapper.writeTree(generator, n);
            } else {
                byte[] data = map.get(s).toString(0).getBytes();
                out.write(data, 0, data.length);
            }

            out.closeEntry();
        }
        out.close();
    }
}
