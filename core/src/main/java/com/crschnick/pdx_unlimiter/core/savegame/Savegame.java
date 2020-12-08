package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.io.JsonConverter;
import com.crschnick.pdx_unlimiter.core.parser.Node;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;

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

public abstract class Savegame {

    private int version;

    private Map<String, Node> nodes;

    protected Savegame(Map<String, Node> nodes, int version) {
        this.nodes = nodes;
        this.version = version;
    }

    public static int getVersion(Path file) throws IOException {
        int v = 0;
        boolean isDir = file.toFile().isDirectory();
        if (!isDir) {
            ZipFile zipFile = new ZipFile(file.toFile());
            var in = zipFile.getInputStream(zipFile.getEntry("version"));
            v = Integer.parseInt(new String(in.readAllBytes()));
            zipFile.close();
        } else {
            v = Integer.parseInt(new String(Files.readAllBytes(file.resolve("version"))));
        }
        return v;
    }

    protected static Map<String, Node> fromFile(Path file, int reqVersion, String... parts) throws IOException {
        Map<String, Node> nodes = new HashMap<>();
        boolean isDir = file.toFile().isDirectory();
        int v = 0;
        if (!isDir) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ObjectMapper mapper = new ObjectMapper();
            for (String s : parts) {
                ZipEntry e = zipFile.getEntry(s + ".json");
                nodes.put(s, JsonConverter.fromJson(mapper.readTree(zipFile.getInputStream(e).readAllBytes())));
            }
            var in = zipFile.getInputStream(zipFile.getEntry("version"));
            v = Integer.parseInt(new String(in.readAllBytes()));
            zipFile.close();
        } else {
            ObjectMapper mapper = new ObjectMapper();
            for (String s : parts) {
                nodes.put(s, JsonConverter.fromJson(mapper.readTree(Files.readAllBytes(file.resolve(s + ".json")))));
            }
            v = Integer.parseInt(new String(Files.readAllBytes(file.resolve("version"))));
        }
        if (v != reqVersion) {
            throw new IOException("Incompatible savegame version " + v + ", required: " + reqVersion);
        }
        return nodes;
    }

    public Map<String, Node> getNodes() {
        return nodes;
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
