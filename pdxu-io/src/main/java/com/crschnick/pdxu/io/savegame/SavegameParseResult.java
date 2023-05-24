package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.Node;

import java.util.Optional;

public abstract class SavegameParseResult {

    public abstract void visit(Visitor visitor);

    public abstract Success orThrow() throws Throwable;

    public Optional<Success> success() {
        return Optional.empty();
    }

    public Optional<Error> error() {
        return Optional.empty();
    }

    public Optional<Invalid> invalid() {
        return Optional.empty();
    }

    public static class Success extends SavegameParseResult {

        public SavegameContent content;

        public Success(SavegameContent content) {
            this.content = content;
        }

        public Node combinedNode() {
            return content.combinedNode();
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.success(this);
        }

        @Override
        public Success orThrow() {
            return this;
        }

        @Override
        public Optional<Success> success() {
            return Optional.of(this);
        }
    }

    public static class Error extends SavegameParseResult {

        public Throwable error;

        public Error(Throwable error) {
            this.error = error;
        }

        @Override
        public Success orThrow() throws Throwable {
            throw error;
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.error(this);
        }

        @Override
        public Optional<Error> error() {
            return Optional.of(this);
        }
    }

    public static class Invalid extends SavegameParseResult {

        public String message;

        public Invalid(String message) {
            this.message = message;
        }

        @Override
        public Success orThrow() throws Exception {
            throw new IllegalArgumentException(message);
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.invalid(this);
        }

        @Override
        public Optional<Invalid> invalid() {
            return Optional.of(this);
        }
    }

    public static abstract class Visitor {

        public void success(Success s) {
        }

        public void error(Error e) {
        }

        public void invalid(Invalid iv) {
        }
    }
}
