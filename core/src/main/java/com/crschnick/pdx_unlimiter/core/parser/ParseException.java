package com.crschnick.pdx_unlimiter.core.parser;

public class ParseException extends Exception {

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String s) {
        super(s);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }
}
