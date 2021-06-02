package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.NodeWriter;
import com.crschnick.pdxu.io.node.ValueNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface SavegameStructure {

    public static final String PDXU_ID_FLAG_NAME = "pdxu_campaign_id";

    SavegameStructure EU4_PLAINTEXT = new PlaintextSavegameStructure(
            "EU4txt".getBytes(),
            "gamestate",
            TextFormatParser.EU4,
            c -> UUID.nameUUIDFromBytes(c.get().getNodeForKey("countries")
                    .getNodeForKey("REB").getNodeForKey("decision_seed").getString().getBytes()),
            (c, id) -> {
                var uuid = UUID.nameUUIDFromBytes(String.valueOf(id).getBytes());
                c.get().getNodeForKey("countries")
                        .getNodeForKey("REB").getNodeForKey("decision_seed")
                        .getValueNode().set(new ValueNode(String.valueOf(id), false));
            });

    SavegameStructure EU4_COMPRESSED = new ZipSavegameStructure(
            "EU4txt".getBytes(),
            TextFormatParser.EU4,
            Set.of(new ZipSavegameStructure.SavegamePart("ai", "ai"),
                    new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "*")),
            c -> UUID.nameUUIDFromBytes(c.get("gamestate").getNodeForKey("countries")
                    .getNodeForKey("REB").getNodeForKey("decision_seed").getString().getBytes()),
            c -> {
                int rand = new Random().nextInt(Integer.MAX_VALUE);
                c.get("gamestate").getNodeForKey("countries")
                        .getNodeForKey("REB").getNodeForKey("decision_seed").getValueNode().set(
                        new ValueNode(String.valueOf(rand), false));
            },
            "rnw.zip");


    SavegameStructure CK3_PLAINTEXT = new Ck3PlaintextSavegameStructure();

    SavegameStructure CK3_COMPRESSED = new Ck3CompressedSavegameStructure();

    SavegameStructure HOI4 = new PlaintextSavegameStructure(
            "HOI4txt".getBytes(),
            "gamestate",
            TextFormatParser.HOI4,
            c -> UUID.fromString(c.get().getNodeForKey("game_unique_id").getString()),
            (c, id) -> {
                var uuid = UUID.nameUUIDFromBytes(String.valueOf(id).getBytes());
                c.get().getNodeForKey("game_unique_id").getValueNode().set(
                        new ValueNode(uuid.toString(), false));
            });


    SavegameStructure STELLARIS = new ZipSavegameStructure(
            null,
            TextFormatParser.STELLARIS,
            Set.of(new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "gamestate")),
            c -> {
                long seed = c.get("gamestate").getNodeForKey("random_seed").getLong();
                byte[] b = new byte[20];
                new Random(seed).nextBytes(b);
                return UUID.nameUUIDFromBytes(b);
            },
            c -> {
                int rand = new Random().nextInt(Integer.MAX_VALUE);
                c.get("gamestate").getNodeForKey("random_seed").getValueNode().set(
                        new ValueNode(String.valueOf(rand), false));
            });


    SavegameStructure CK2_PLAINTEXT = new PlaintextSavegameStructure(
            "CK2txt".getBytes(),
            "gamestate",
            TextFormatParser.CK2,
            c -> {
                long seed = c.get().getNodeForKey("playthrough_id").getLong();
                byte[] b = new byte[20];
                new Random(seed).nextBytes(b);
                return UUID.nameUUIDFromBytes(b);
            },
            (c,id) -> c.get().getNodeForKey("playthrough_id").getValueNode().set(
                    new ValueNode(String.valueOf(id), false))) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getParser().getCharset(), node, "\t", 1);
            out.write("}".getBytes());
        }
    };

    SavegameStructure CK2_COMPRESSED = new ZipSavegameStructure(
            "CK2txt".getBytes(),
            TextFormatParser.CK2,
            Set.of(new ZipSavegameStructure.SavegamePart("meta", "meta"),
                    new ZipSavegameStructure.SavegamePart("gamestate", "*")),
            c -> {
                long seed = c.get("gamestate").getNodeForKey("playthrough_id").getLong();
                byte[] b = new byte[20];
                new Random(seed).nextBytes(b);
                return UUID.nameUUIDFromBytes(b);
            },
            c -> c.get("gamestate").getNodeForKey("playthrough_id").getValueNode().set(
                    new ValueNode(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)), false))) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getParser().getCharset(), node, "\t", 1);
            out.write("\n}".getBytes());
        }
    };


    private static Optional<UUID> getIdFromPreviousWar(SavegameContent c) {
        AtomicReference<UUID> uuid = new AtomicReference<>();
        c.get().getNodesForKey("previous_war").forEach(n -> {
            n.getNodeForKeyIfExistent("history").ifPresent(h -> {
                h.forEach((date, entry) -> {
                    if (entry.hasKey("battle")) {
                        var losses = entry.getNodeForKey("battle").getNodeForKey("attacker").getNodeForKey("losses");
                        uuid.set(UUID.nameUUIDFromBytes(losses.getString().getBytes()));
                    }
                });
            });
        });
        return Optional.ofNullable(uuid.get());
    }

    private static void setIdForPreviousWar(SavegameContent c, long id) {
        AtomicBoolean found = new AtomicBoolean();
        c.get().getNodesForKey("previous_war").forEach(n -> {
            n.getNodeForKeyIfExistent("history").ifPresent(h -> {
                h.forEach((date, entry) -> {
                    if (entry.hasKey("battle") && !found.get()) {
                        var losses = entry.getNodeForKey("battle").getNodeForKey("attacker").getNodeForKey("losses");
                        losses.getValueNode().set(new ValueNode(String.valueOf(id), false));
                        found.set(true);
                    }
                });
            });
        });
    }

    SavegameStructure VIC2 = new PlaintextSavegameStructure(
            null,
            "gamestate",
            TextFormatParser.VIC2,
            c -> getIdFromPreviousWar(c).get(),
            (c,id) -> {
                setIdForPreviousWar(c, id);
            }) {

        @Override
        public void writeData(OutputStream out, ArrayNode node) throws IOException {
            NodeWriter.write(out, getParser().getCharset(), node, "\t", 0);
            out.write("}".getBytes());
        }
    };

    static boolean validateHeader(byte[] header, byte[] content) {
        if (content.length < header.length) {
            return false;
        }

        byte[] first = new byte[header.length];
        System.arraycopy(content, 0, first, 0, header.length);
        return Arrays.equals(first, header);
    }

    void write(Path out, SavegameContent c) throws IOException;

    default void writeData(OutputStream out, ArrayNode node) throws IOException {
        NodeWriter.write(out, getParser().getCharset(), node, "\t", 0);
    }

    UUID getCampaignIdHeuristic(SavegameContent c);

    void generateNewCampaignIdHeuristic(SavegameContent c, Long id);

    SavegameParseResult parse(byte[] input);

    TextFormatParser getParser();
}
