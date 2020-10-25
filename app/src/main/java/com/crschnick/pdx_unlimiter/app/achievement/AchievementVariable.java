package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

import java.util.List;

public abstract class AchievementVariable {

    public static AchievementVariable fromNode(String name, JsonNode json) {
        String type = json.get("type").textValue();
        if (type.equals("pathValue")) {
            return new PathVariable(name, json.get("node").textValue(), json.get("path").textValue(), json.get("list").asBoolean());
        } else {
            throw new IllegalArgumentException("Invalid variable type: " + type);
        }
    }

    private String name;

    public AchievementVariable(String name) {
        this.name = name;
    }

    public abstract String getExpression();

    public abstract String evaluate(Eu4IntermediateSavegame sg, String expression);

    public String getName() {
        return name;
    }

    public static class ValueVariable extends AchievementVariable {

        private String value;

        public ValueVariable(String name, String value) {
            super(name);
            this.value = value;
        }

        @Override
        public String getExpression() {
            return value;
        }

        @Override
        public String evaluate(Eu4IntermediateSavegame sg, String expression) {
            return expression;
        }
    }

    public static class PathVariable extends AchievementVariable {

        private String node;
        private String path;
        private boolean list;

        public PathVariable(String name, String node, String path, boolean list) {
            super(name);
            this.node = node;
            this.path = path;
            this.list = list;
        }

        @Override
        public String getExpression() {
            return path;
        }

        private String listToString(List<Node> nodes) {
            if (nodes.size() == 0) {
                return "[]";
            }

            Object v = ((ValueNode) nodes.get(0)).getValue();
            if (v instanceof String) {
                return "[" + String.join(", ", nodes.stream()
                        .map(n-> "'" + Node.getString(n) + "'")
                        .toArray(String[]::new)) + "]";
            } else if (v instanceof Long || v instanceof Boolean || v instanceof Double){
                return "[" + nodes.toString() + "]";
            } else {
                throw new IllegalArgumentException("Invalid return type for path result " + nodes.toString());
            }
        }

        @Override
        public String evaluate(Eu4IntermediateSavegame sg, String expression) {
            ArrayNode r;
            try {
                r = JsonPath.read(sg.getNodes().get(node), expression);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid path " + expression, e);
            }

            if (list) {
                return listToString(r.getNodes());
            } else {
                if (r.getNodes().size() != 1) {
                    throw new IllegalArgumentException("List result for non list variable with path " + expression);
                }

                Object value = ((ValueNode) r.getNodes().get(0)).getValue();
                return value instanceof String ? "'" + value + "'" : value.toString();
            }
        }
    }

    public static class FilterCountVariable extends AchievementVariable {

        private String node;
        private String filter;

        public FilterCountVariable(String name, String node, String filter) {
            super(name);
            this.node = node;
            this.filter = filter;
        }

        @Override
        public String getExpression() {
            return filter;
        }

        @Override
        public String evaluate(Eu4IntermediateSavegame sg, String expression) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), expression);
            return String.valueOf(r.getNodes().size());
        }
    }
}
