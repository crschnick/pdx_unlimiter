package com.crschnick.pdxu.io.parser;

public class ParseException extends Exception {

    private int offset;
    private String excerpt;

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(String s, int offset, byte[] data) {
        super(s);
    }
}
