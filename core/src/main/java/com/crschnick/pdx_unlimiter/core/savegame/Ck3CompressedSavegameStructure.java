package com.crschnick.pdx_unlimiter.core.savegame;

import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Ck3CompressedSavegameStructure extends ZipSavegameStructure {

    public Ck3CompressedSavegameStructure() {
        super(null, StandardCharsets.UTF_8, Set.of(new SavegamePart("gamestate", "gamestate")));
    }

    @Override
    public SavegameParseResult parse(byte[] input) {
        var header = Ck3Header.fromStartOfFile(input);
        if (header.binary()) {
            throw new IllegalArgumentException("Binary savegames are not supported");
        }
        if (!header.compressed()) {
            throw new IllegalArgumentException("Uncompressed savegames are not supported");
        }

        int metaStart = header.toString().length() + 1;
        int contentStart = (int) (metaStart + header.metaLength()) + 1;
        return parseInput(input, contentStart);
    }
}
