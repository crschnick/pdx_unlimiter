package com.paradox_challenges.eu4_unlimiter.parser.eu4;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.paradox_challenges.eu4_unlimiter.format.NodeSplitter;
import com.paradox_challenges.eu4_unlimiter.io.JsonConverter;
import com.paradox_challenges.eu4_unlimiter.parser.GamedataParser;
import com.paradox_challenges.eu4_unlimiter.parser.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Eu4Savegame {

    private Node gamestate;
    private Node ai;
    private Node meta;

    public Eu4Savegame(Node gamestate, Node ai, Node meta) {
        this.gamestate = gamestate;
        this.ai = ai;
        this.meta = meta;
    }

    private static final GamedataParser normalParser = new Eu4NormalParser();
    private static final GamedataParser ironmanParser = new Eu4IronmanParser();

    public static Eu4Savegame fromFile(Path file) throws IOException {
        boolean isZipped = new ZipInputStream(Files.newInputStream(file)).getNextEntry() != null;
        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ZipEntry gamestate = zipFile.getEntry("gamestate");
            ZipEntry meta = zipFile.getEntry("meta");
            ZipEntry ai = zipFile.getEntry("ai");

            Optional<Node> gamestateNode = ironmanParser.parse(zipFile.getInputStream(gamestate));
            if (gamestateNode.isPresent()) {
                Node metaNode = ironmanParser.parse(zipFile.getInputStream(meta)).get();
                Node aiNode =ironmanParser.parse(zipFile.getInputStream(ai)).get();
                return new Eu4Savegame(gamestateNode.get(), aiNode, metaNode);
            } else {
                return new Eu4Savegame(normalParser.parse(zipFile.getInputStream(gamestate)).get(),
                        normalParser.parse(zipFile.getInputStream(ai)).get(),
                        normalParser.parse(zipFile.getInputStream(meta)).get());
            }
        } else {
            throw new IOException();
            //Optional<Node> node = normalParser.parse(Files.newInputStream(file));
            //return new Eu4Savegame(node.get(), node.get(), node.get());
        }
            }

    public void write(String fileName, boolean txtSuffix) throws IOException {
        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e1 = new ZipEntry("gamestate" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e1);
        byte[] b1 = gamestate.toString(0).getBytes();
        out.write(b1, 0, b1.length);
        out.closeEntry();

        ZipEntry e2 = new ZipEntry("meta" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e2);
        byte[] b2 = meta.toString(0).getBytes();
        out.write(b2, 0, b2.length);
        out.closeEntry();

        ZipEntry e3 = new ZipEntry("ai" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e3);
        byte[] b3 = ai.toString(0).getBytes();
        out.write(b3, 0, b3.length);
        out.closeEntry();
        out.close();
    }

    public Node getGamestate() {
        return gamestate;
    }

    public Node getAi() {
        return ai;
    }

    public Node getMeta() {
        return meta;
    }
}
