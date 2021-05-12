package com.crschnick.pdxu.io.savegame;

import com.crschnick.pdxu.io.node.ArrayNode;
import com.crschnick.pdxu.io.node.LinkedArrayNode;
import com.crschnick.pdxu.io.node.Node;

import java.util.Map;

public abstract class SavegameParseResult {

    @SuppressWarnings("unchecked")
    public abstract void visit(Visitor visitor);

    public static class Success extends SavegameParseResult {

        public Map<String, ArrayNode> content;

        public Success(Map<String, ArrayNode> content) {
            this.content = content;
        }

        public Node combinedNode() {
            return new LinkedArrayNode(content.values().stream().toList());
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.success(this);
        }
    }

    public static class Error extends SavegameParseResult {

        public Throwable error;

        public Error(Throwable error) {
            this.error = error;
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.error(this);
        }
    }

    public static class Invalid extends SavegameParseResult {

        public String message;

        public Invalid(String message) {
            this.message = message;
        }

        @Override
        public void visit(Visitor visitor) {
            visitor.invalid(this);
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
