package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.parser.BinaryFormatParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.ZipInputStream;

public class Ck3RawSavegame extends RawSavegame {


    private Node gamestate;
    private Node meta;

    public Ck3RawSavegame(String checksum, Node gamestate, Node meta) {
        super(checksum);
        this.gamestate = gamestate;
        this.meta = meta;
    }

    public static Ck3RawSavegame fromFile(Path file) throws Exception {
        var in = Files.newInputStream(file);
        var content = in.readAllBytes();
        var contentString = new String(content, StandardCharsets.UTF_8);
        String first = contentString.lines().findFirst().get();
        if (!first.matches("SAV\\w+")) {
            throw new SavegameParseException("Header is not valid");
        }

        MessageDigest d = MessageDigest.getInstance("MD5");
        d.update(content);
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        String checksum = c.toString();

        boolean isZipped = new ZipInputStream(new ByteArrayInputStream(content)).getNextEntry() != null;

        return parseContent(content, checksum);
    }

    private static Ck3RawSavegame parseContent(byte[] content, String checksum) throws Exception {
        var contentString = new String(content, StandardCharsets.UTF_8);
        String first = contentString.lines().findFirst().get();
        int metaStart = first.length() + 1;
        boolean binary = !contentString.startsWith("meta", metaStart);
        int metaEnd = binary ? indexOf(content, "PK".getBytes()) : (indexOf(content, "}\nPK".getBytes()) + 2);
        byte[] metaContent = Arrays.copyOfRange(content, metaStart, metaEnd);
        byte[] zipContent = Arrays.copyOfRange(content, metaEnd, content.length);
        var zipIn = new ZipInputStream(new ByteArrayInputStream(zipContent));
        var gamestateEntry = zipIn.getNextEntry();
        var n = gamestateEntry.getName();
        boolean isZipped = gamestateEntry != null;

        if (!isZipped) {
            throw new SavegameParseException("Ck3 gamestate must be zipped");
        }

        var parser = binary ? BinaryFormatParser.ck3Parser() : TextFormatParser.textFileParser();
        Node gamestateNode = parser.parse(zipIn).get();
        zipIn.close();

        var duplicateMeta = Node.getKeyValueNodeForKey(gamestateNode, "meta_data");
        Node.getNodeArray(gamestateNode).remove(duplicateMeta);

        return new Ck3RawSavegame(checksum, gamestateNode, duplicateMeta.getNode());
    }

    private static int indexOf(byte[] array, byte[] toFind) {
        for(int i = 0; i < array.length - toFind.length + 1; ++i) {
            boolean found = true;
            for (int j = 0; j < toFind.length; ++j) {
                if (array[i+j] != toFind[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        throw new IllegalArgumentException("Array not found");
    }

    public Node getGamestate() {
        return gamestate;
    }

    public Node getMeta() {
        return meta;
    }
}
