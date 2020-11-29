package com.crschnick.pdx_unlimiter.eu4.savegame;

import com.crschnick.pdx_unlimiter.eu4.format.Namespace;
import com.crschnick.pdx_unlimiter.eu4.format.NodeSplitter;
import com.crschnick.pdx_unlimiter.eu4.io.SavegameWriter;
import com.crschnick.pdx_unlimiter.eu4.parser.BinaryFormatParser;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.TextFormatParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class StellarisRawSavegame extends RawSavegame {

    private Node gamestate;
    private Node meta;

    public StellarisRawSavegame(String checksum, Node gamestate, Node meta) {
        super(checksum);
        this.gamestate = gamestate;
        this.meta = meta;
    }

    public static StellarisRawSavegame fromFile(Path file) throws Exception {
        var in = Files.newInputStream(file);
        boolean isZipped = new ZipInputStream(in).getNextEntry() != null;
        in.close();

        if (!isZipped) {
            throw new SavegameParseException("Stellaris savegame must be zipped");
        }

        MessageDigest d = MessageDigest.getInstance("MD5");
        d.update(Files.readAllBytes(file));
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        String checksum = c.toString();

        ZipFile zipFile = new ZipFile(file.toFile());
        ZipEntry gamestate = zipFile.getEntry("gamestate");
        ZipEntry meta = zipFile.getEntry("meta");

        Node gamestateNode = TextFormatParser.stellarisSavegameParser().parse(zipFile.getInputStream(gamestate)).get();
        Node metaNode = TextFormatParser.stellarisSavegameParser().parse(zipFile.getInputStream(meta)).get();
        return new StellarisRawSavegame(checksum, gamestateNode, metaNode);
    }

    public Node getGamestate() {
        return gamestate;
    }

    public Node getMeta() {
        return meta;
    }
}
