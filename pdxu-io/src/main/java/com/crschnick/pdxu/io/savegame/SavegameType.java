package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ValueNode;
import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.zip.ZipInputStream;

public interface SavegameType {

    SavegameType EU4 = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            if (isCompressed(input)) {
                return SavegameStructure.EU4_COMPRESSED;
            } else {
                return SavegameStructure.EU4_PLAINTEXT;
            }
        }

        @Override
        public boolean isCompressed(byte[] input) {
            try {
                var zipIn = new ZipInputStream(new ByteArrayInputStream(input));
                return zipIn.getNextEntry() != null;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        public String getFileEnding() {
            return "eu4";
        }

        @Override
        public boolean isBinary(byte[] input) {
            // Check for uncompressed binary
            if (SavegameStructure.validateHeader("EU4bin".getBytes(StandardCharsets.US_ASCII), input)) {
                return true;
            }

            try {
                var zipIn = new ZipInputStream(new ByteArrayInputStream(input));
                zipIn.getNextEntry();
                var header = zipIn.readNBytes(6);
                return new String(header).equals("EU4bin");
            } catch (IOException ex) {
                return false;
            }
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.eu4();
        }

        public UUID getCampaignIdHeuristic(SavegameContent c) {
            return UUID.nameUUIDFromBytes(c.get().getNodeForKey("countries")
                                                  .getNodeForKey("REB").getNodeForKey("decision_seed").getString().getBytes());
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {
            int rand = new Random().nextInt(Integer.MAX_VALUE);
            c.get().getNodeForKey("countries")
                    .getNodeForKey("REB").getNodeForKey("decision_seed").getValueNode().set(
                            new ValueNode(String.valueOf(rand), false));
        }
    };

    SavegameType HOI4 = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            return SavegameStructure.HOI4;
        }

        @Override
        public boolean isCompressed(byte[] input) {
            return false;
        }

        @Override
        public String getFileEnding() {
            return "hoi4";
        }

        @Override
        public boolean isBinary(byte[] input) {
            var header = Arrays.copyOfRange(input, 0, 7);
            return new String(header).equals("HOI4bin");
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.hoi4();
        }

        public UUID getCampaignIdHeuristic(SavegameContent c) {
            return UUID.fromString(c.get().getNodeForKey("game_unique_id").getString());
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {
            c.get().getNodeForKey("game_unique_id").getValueNode().set(
                    new ValueNode(UUID.randomUUID().toString(), false));
        }
    };

    SavegameType VIC3 = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            if (isSplitCompressed(input)) {
                return SavegameStructure.VIC3_SPLIT_COMPRESSED;
            } else if (isUnifiedCompressed(input)) {
                return SavegameStructure.VIC3_UNIFIED_COMPRESSED;
            } else {
                return SavegameStructure.VIC3_PLAINTEXT;
            }
        }

        @Override
        public boolean isCompressed(byte[] input) {
            if (ModernHeader.skipsHeader(input)) {
                return ModernHeaderCompressedSavegameStructure.indexOfCompressedGamestateStart(input) != -1;
            }

            var header = ModernHeader.determineHeaderForFile(input);
            return header.isCompressed();
        }

        public boolean isUnifiedCompressed(byte[] input) {
            if (ModernHeader.skipsHeader(input)) {
                return ModernHeaderCompressedSavegameStructure.indexOfCompressedGamestateStart(input) != -1;
            }

            var header = ModernHeader.determineHeaderForFile(input);
            return header.isUnifiedCompressed();
        }

        public boolean isSplitCompressed(byte[] input) {
            if (ModernHeader.skipsHeader(input)) {
                return ModernHeaderCompressedSavegameStructure.indexOfCompressedGamestateStart(input) != -1;
            }

            var header = ModernHeader.determineHeaderForFile(input);
            return header.isSplitCompressed();
        }

        @Override
        public String getFileEnding() {
            return "v3";
        }

        @Override
        public boolean isBinary(byte[] input) {
            if (ModernHeader.skipsHeader(input)) {
                return false;
            }

            return ModernHeader.determineHeaderForFile(input).binary();
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.vic3();
        }

        public UUID getCampaignIdHeuristic(SavegameContent c) {
            var seed = UUID.fromString(c.get().getNodeForKey("playthrough_id").getString());
            return seed;
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {
            c.get("gamestate").getNodeForKey("playthrough_id").getValueNode().set(
                    new ValueNode(UUID.randomUUID().toString(), true));
        }
    };

    SavegameType CK3 = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            if (isCompressed(input)) {
                return SavegameStructure.CK3_COMPRESSED;
            } else {
                return SavegameStructure.CK3_PLAINTEXT;
            }
        }

        @Override
        public boolean isCompressed(byte[] input) {
            if (ModernHeader.skipsHeader(input)) {
                return ModernHeaderCompressedSavegameStructure.indexOfCompressedGamestateStart(input) != -1;
            }

            var header = ModernHeader.determineHeaderForFile(input);
            return header.isCompressed();
        }

        @Override
        public String getFileEnding() {
            return "ck3";
        }

        @Override
        public boolean isBinary(byte[] input) {
            if (ModernHeader.skipsHeader(input)) {
                return false;
            }

            return ModernHeader.determineHeaderForFile(input).binary();
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.ck3();
        }

        @Override
        public UUID getCampaignIdHeuristic(SavegameContent c) {
            long seed = c.get().getNodeForKey("random_seed").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            return UUID.nameUUIDFromBytes(b);
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {
            int rand = new Random().nextInt(Integer.MAX_VALUE);
            c.get().getNodeForKey("random_seed").getValueNode().set(
                    new ValueNode(String.valueOf(rand), false));
        }
    };


    SavegameType STELLARIS = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            return SavegameStructure.STELLARIS;
        }

        @Override
        public boolean isCompressed(byte[] input) {
            return true;
        }

        @Override
        public String getFileEnding() {
            return "sav";
        }

        @Override
        public boolean isBinary(byte[] input) {
            return false;
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.stellaris();
        }

        public UUID getCampaignIdHeuristic(SavegameContent c) {
            long seed = c.get("gamestate").getNodeForKey("random_seed").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            return UUID.nameUUIDFromBytes(b);
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {
            int rand = new Random().nextInt(Integer.MAX_VALUE);
            c.get("gamestate").getNodeForKey("random_seed").getValueNode().set(
                    new ValueNode(String.valueOf(rand), false));
        }
    };

    SavegameType CK2 = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            return isCompressed(input) ? SavegameStructure.CK2_COMPRESSED : SavegameStructure.CK2_PLAINTEXT;
        }

        @Override
        public boolean isCompressed(byte[] input) {
            try {
                var zipIn = new ZipInputStream(new ByteArrayInputStream(input));
                return zipIn.getNextEntry() != null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String getFileEnding() {
            return "ck2";
        }

        @Override
        public boolean isBinary(byte[] input) {
            return false;
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.ck2();
        }

        public UUID getCampaignIdHeuristic(SavegameContent c) {
            long seed = c.get().getNodeForKey("playthrough_id").getLong();
            byte[] b = new byte[20];
            new Random(seed).nextBytes(b);
            return UUID.nameUUIDFromBytes(b);
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {
            c.get("gamestate").getNodeForKey("playthrough_id").getValueNode().set(
                    new ValueNode(String.valueOf(new Random().nextInt(Integer.MAX_VALUE)), false));
        }
    };

    SavegameType VIC2 = new SavegameType() {

        @Override
        public SavegameStructure determineStructure(byte[] input) {
            return SavegameStructure.VIC2;
        }

        @Override
        public boolean isCompressed(byte[] input) {
            return false;
        }

        @Override
        public String getFileEnding() {
            return "v2";
        }

        @Override
        public boolean isBinary(byte[] input) {
            return false;
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.vic2();
        }

        public UUID getCampaignIdHeuristic(SavegameContent c) {
            var p = c.get().getNodeForKey("player").getString();
            return UUID.nameUUIDFromBytes(p.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void generateNewCampaignIdHeuristic(SavegameContent c) {

        }
    };

    static SavegameType getTypeForFile(Path path) {
        for (var ft : SavegameType.class.getFields()) {
            try {
                SavegameType t = (SavegameType) ft.get(null);
                if (path.getFileName().toString().endsWith("." + t.getFileEnding())) {
                    return t;
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        return null;
    }

    SavegameStructure determineStructure(byte[] input);

    boolean isCompressed(byte[] input);

    String getFileEnding();

    boolean isBinary(byte[] input);

    TextFormatParser getParser();

    UUID getCampaignIdHeuristic(SavegameContent c);

    void generateNewCampaignIdHeuristic(SavegameContent c);
}
