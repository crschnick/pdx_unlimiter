package com.crschnick.pdxu.io.parser;

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
