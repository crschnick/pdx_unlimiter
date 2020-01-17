package com.paradox_challenges.eu4_unlimiter.converter;

import com.paradox_challenges.eu4_unlimiter.format.NodeSplitter;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SavegameConverter {

    private String gamestateName;

    private String[] names;

    public SavegameConverter(String gamestateName, String... names) {
        this.gamestateName = gamestateName;
        this.names = names;
    }

    public void writeToFile(Node node, String fileName) throws IOException {
        Map<String, Node> map = new NodeSplitter(names).removeNodes(node);

        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        for (String s : names) {
            ZipEntry e = new ZipEntry(s + ".json");
            out.putNextEntry(e);
            byte[] data = map.get(s).toString(0).getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
        }

        ZipEntry e = new ZipEntry(gamestateName + ".json");
        out.putNextEntry(e);
        byte[] data = node.toString(0).getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();

        out.close();
    }
}
