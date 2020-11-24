package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.io.SavegameWriter;
import com.crschnick.pdx_unlimiter.eu4.parser.BinaryFormatParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.TextFormatParser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;

public class Hoi4RawSavegame extends RawSavegame {

    private Node content;

    public Hoi4RawSavegame(String checksum, Node content) {
        super(checksum);
        this.content = content;
    }

    public static Hoi4RawSavegame fromFile(Path file) throws Exception {
        MessageDigest d = MessageDigest.getInstance("MD5");
        d.update(Files.readAllBytes(file));
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        String checksum = c.toString();

        Optional<Node> node = BinaryFormatParser.hoi4Parser().parse(Files.newInputStream(file));
        if (node.isPresent()) {
            Node content = node.get();
            return new Hoi4RawSavegame(checksum, content);
        } else {
            return new Hoi4RawSavegame(checksum, TextFormatParser.hoi4SavegameParser().parse(Files.newInputStream(file)).get());
        }

    }

    public void write(String fileName) throws IOException {
        File f = new File(fileName);
        OutputStream out = Files.newOutputStream(f.toPath());
        SavegameWriter.writeNode(content, out);
        out.close();
    }

    public Node getContent() {
        return content;
    }
}
