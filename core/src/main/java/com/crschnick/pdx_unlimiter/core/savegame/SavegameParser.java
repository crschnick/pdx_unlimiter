package com.crschnick.pdx_unlimiter.core.savegame;

import com.crschnick.pdx_unlimiter.core.parser.Node;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class SavegameParser<I extends SavegameInfo<?>> {

    public abstract Status parse(Path input, Melter melter);

    public String checksum(byte[] content) {
        MessageDigest d = null;
        try {
            d = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 missing!");
        }
        d.update(content);
        StringBuilder c = new StringBuilder();
        ByteBuffer b = ByteBuffer.wrap(d.digest());
        for (int i = 0; i < 16; i++) {
            var hex = String.format("%02x", b.get());
            c.append(hex);
        }
        String checksum = c.toString();
        return checksum;
    }

    @FunctionalInterface
    public static interface Melter {
        Path melt(Path input) throws IOException;
    }

    public static abstract class Status {

        public void visit(StatusVisitor visitor) {
            if (this instanceof Success) {
                visitor.success((Success) this);
            }
            if (this instanceof Error) {
                visitor.error((Error) this);
            }
            if (this instanceof Invalid) {
                visitor.invalid((Invalid) this);
            }
        }
    }

    public static class Success<I extends SavegameInfo<?>> extends Status {

        public boolean binary;
        public String checksum;
        public Node content;
        public I info;

        public Success(boolean binary, String checksum, Node content, I info) {
            this.binary = binary;
            this.checksum = checksum;
            this.content = content;
            this.info = info;
        }
    }

    public static class Error extends Status {

        public Throwable error;

        public Error(Throwable error) {
            this.error = error;
        }
    }

    public static class Invalid extends Status {

        public String message;

        public Invalid(String message) {
            this.message = message;
        }
    }

    public static abstract class StatusVisitor<I extends SavegameInfo<?>> {

        public void success(Success<I> s) {
        }

        public void error(Error e) {
        }

        public void invalid(Invalid iv) {
        }
    }
}
