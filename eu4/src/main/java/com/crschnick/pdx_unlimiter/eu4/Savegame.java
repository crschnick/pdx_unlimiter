package com.crschnick.pdx_unlimiter.eu4;

public class Savegame {

    protected String fileChecksum;

    public Savegame(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }
}
