package com.crschnick.pdx_unlimiter.eu4.savegame;

public class RawSavegame {

    protected String fileChecksum;

    public RawSavegame(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }
}
