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

public class Eu4RawSavegame extends RawSavegame {

    private static final String[] META_NODES = new String[]{"date", "save_game", "player",
            "displayed_country_name", "savegame_version", "savegame_versions", "dlc_enabled",
            "multi_player", "not_observer", "campaign_id", "campaign_length", "campaign_stats",
            "is_random_new_world", "ironman"};
    private static final String[] AI_NODES = new String[]{"ai"};

    private Node gamestate;
    private Node ai;
    private Node meta;

    public Eu4RawSavegame(String checksum, Node gamestate, Node ai, Node meta) {
        super(checksum);
        this.gamestate = gamestate;
        this.ai = ai;
        this.meta = meta;
    }

    public static Eu4RawSavegame fromFile(Path file) throws Exception {
        var in = Files.newInputStream(file);
        boolean isZipped = new ZipInputStream(in).getNextEntry() != null;
        in.close();

        MessageDigest d = MessageDigest.getInstance("MD5");
        d.update(Files.readAllBytes(file));
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        String checksum = c.toString();

        if (isZipped) {
            ZipFile zipFile = new ZipFile(file.toFile());
            ZipEntry gamestate = zipFile.getEntry("gamestate");
            ZipEntry meta = zipFile.getEntry("meta");
            ZipEntry ai = zipFile.getEntry("ai");

            Optional<Node> gamestateNode = BinaryFormatParser.eu4Parser(Namespace.EU4_GAMESTATE).parse(zipFile.getInputStream(gamestate));
            if (gamestateNode.isPresent()) {
                Node metaNode = BinaryFormatParser.eu4Parser(Namespace.EU4_META).parse(zipFile.getInputStream(meta)).get();
                Node aiNode = BinaryFormatParser.eu4Parser(Namespace.EU4_AI).parse(zipFile.getInputStream(ai)).get();
                zipFile.close();
                return new Eu4RawSavegame(checksum, gamestateNode.get(), aiNode, metaNode);
            } else {
                var s = new Eu4RawSavegame(checksum, TextFormatParser.eu4SavegameParser().parse(zipFile.getInputStream(gamestate)).get(),
                        TextFormatParser.eu4SavegameParser().parse(zipFile.getInputStream(ai)).get(),
                        TextFormatParser.eu4SavegameParser().parse(zipFile.getInputStream(meta)).get());
                zipFile.close();
                return s;
            }
        } else {
            Optional<Node> node = TextFormatParser.eu4SavegameParser().parse(Files.newInputStream(file));
            if (node.isPresent()) {
                Node meta = new NodeSplitter(META_NODES).splitFromNode(node.get());
                Node ai = new NodeSplitter(AI_NODES).splitFromNode(node.get());
                return new Eu4RawSavegame(checksum, node.get(), ai, meta);
            }
            throw new IOException("Invalid savegame: " + file.toString());
        }
    }

    public void write(String fileName, boolean txtSuffix) throws IOException {
        File f = new File(fileName);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e1 = new ZipEntry("gamestate" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e1);
        SavegameWriter.writeNode(gamestate, out);
        out.closeEntry();

        ZipEntry e2 = new ZipEntry("meta" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e2);
        SavegameWriter.writeNode(meta, out);
        out.closeEntry();

        ZipEntry e3 = new ZipEntry("ai" + (txtSuffix ? ".txt" : ""));
        out.putNextEntry(e3);
        SavegameWriter.writeNode(ai, out);
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
