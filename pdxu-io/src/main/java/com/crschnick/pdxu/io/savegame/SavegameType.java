package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.parser.TextFormatParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
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
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String getFileEnding() {
            return "eu4";
        }

        @Override
        public boolean isBinary(byte[] input) {
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
            var header = Ck3Header.fromStartOfFile(input);
            return header.compressed();
        }

        @Override
        public String getFileEnding() {
            return "ck3";
        }

        @Override
        public boolean isBinary(byte[] input) {
            var header = Arrays.copyOfRange(input, 0, Ck3Header.LENGTH);
            return Ck3Header.fromStartOfFile(header).binary();
        }

        @Override
        public TextFormatParser getParser() {
            return TextFormatParser.ck3();
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
}
