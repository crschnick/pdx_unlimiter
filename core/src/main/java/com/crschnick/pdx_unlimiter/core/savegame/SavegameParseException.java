package com.crschnick.pdx_unlimiter.core.savegame;

public class SavegameParseException extends Exception {

    public SavegameParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public SavegameParseException(String s) {
        super(s);
    }
}
