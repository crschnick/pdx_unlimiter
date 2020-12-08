package com.crschnick.pdx_unlimiter.core.savegame;

public class RawSavegame {

    protected String fileChecksum;

    public RawSavegame(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }
}
