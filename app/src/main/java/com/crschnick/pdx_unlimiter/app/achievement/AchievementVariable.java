package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AchievementVariable {

    public static AchievementVariable fromNode(String name, JsonNode json) {
        String type = json.required("type").textValue();
        if (type.equals("pathValue")) {
            return new PathVariable(name,
                    json.required("node").textValue(),
                    json.required("path").textValue(),
                    json.required("list").asBoolean());
        } else if (type.equals("value")) {
            return new ValueVariable(name, json.required("value").textValue());
        } else if (type.equals("pathCount")) {
            return new PathCountVariable(name, json.required("node").textValue(), json.required("path").textValue());
        } else {
            throw new IllegalArgumentException("Invalid variable type: " + type);
        }
    }

    public static String applyVariables(Map<AchievementVariable,String> expr, String input) {
        String s = input;
        for (var e : expr.entrySet()) {
            s = s.replace("%{" + e.getKey().getName() + "}", e.getValue());
        }
        return s;
    }


    public static Map<AchievementVariable,String> evaluateVariables(List<AchievementVariable> variables, Map<String,Node> nodes) {
        Map<AchievementVariable,String> expr = new HashMap<>();
        for (AchievementVariable v : variables) {
            String currentVar = AchievementVariable.applyVariables(expr, v.getExpression());
            String eval = v.evaluate(nodes, currentVar);
            expr.put(v, eval);
            LoggerFactory.getLogger(Achievement.class).debug(
                    "Evaluating variable"
                            + "\n    name: " + v.getName()
                            + "\n    expression:  " + currentVar
                            + "\n    evaluation: " + eval);
        }
        return expr;
    }

    private String name;

    public AchievementVariable(String name) {
        this.name = name;
    }

    public abstract String getExpression();

    public abstract String evaluate(Map<String,Node> nodes, String expression);

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
        public String evaluate(Map<String,Node> nodes, String expression) {
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
                return "[" + nodes.stream().map(n -> ((ValueNode) n).getValue().toString()).collect(Collectors.joining(","))+ "]";
            } else {
                throw new IllegalArgumentException("Invalid return type for path result " + nodes.toString());
            }
        }

        @Override
        public String evaluate(Map<String,Node> nodes, String expression) {
            ArrayNode r;
            try {
                JsonPath p = JsonPath.compile(expression);
                r = p.read(nodes.get(node));
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

    public static class PathCountVariable extends AchievementVariable {

        private String node;
        private String filter;

        public PathCountVariable(String name, String node, String filter) {
            super(name);
            this.node = node;
            this.filter = filter;
        }

        @Override
        public String getExpression() {
            return filter;
        }

        @Override
        public String evaluate(Map<String,Node> nodes, String expression) {
            ArrayNode r = JsonPath.read(nodes.get(node), expression);
            return String.valueOf(r.getNodes().size());
        }
    }
}
