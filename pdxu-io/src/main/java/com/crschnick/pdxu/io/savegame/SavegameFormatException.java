package com.crschnick.pdxu.io.savegame;

public class SavegameFormatException extends RuntimeException {

    public SavegameFormatException() {
    }

    public SavegameFormatException(String message) {
        super(message);
    }

    public SavegameFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public SavegameFormatException(Throwable cause) {
        super(cause);
    }

    public SavegameFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
