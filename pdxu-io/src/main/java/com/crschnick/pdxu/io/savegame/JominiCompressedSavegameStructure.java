package com.crschnick.pdxu.io.savegame;

import java.util.Set;

abstract class JominiCompressedSavegameStructure extends ZipSavegameStructure {

    private static final int MAX_SEARCH = 150000;
    static final byte[] ZIP_HEADER = new byte[] {0x50, 0x4B, 0x03, 0x04};

    protected JominiCompressedSavegameStructure(SavegameType type, Set<SavegamePart> parts) {
        super(null, type, parts);
    }

    static int indexOfCompressedGamestateStart(byte[] array) {
        if (array.length < ZIP_HEADER.length) {
            return -1;
        }

        int end = Math.min(array.length, MAX_SEARCH - ZIP_HEADER.length);
        for (int i = 0; i < end; ++i) {
            boolean found = true;
            for (int j = 0; j < ZIP_HEADER.length; ++j) {
                if (array[i + j] != ZIP_HEADER[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }
}
