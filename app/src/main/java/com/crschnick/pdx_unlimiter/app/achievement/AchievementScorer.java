package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public interface AchievementScorer {

    static AchievementScorer fromJsonNode(JsonNode node, AchievementContent content) {
        return scorerFromJsonNode(node, content);
    }

    private static List<AchievementScorer> scorersFromJsonNode(JsonNode node, AchievementContent content) {
        List<AchievementScorer> scorers = new ArrayList<>();
        node.elements().forEachRemaining(n -> {
            scorers.add(scorerFromJsonNode(n, content));
        });
        return scorers;
    }


    private static AchievementScorer scorerFromJsonNode(JsonNode n, AchievementContent content) {
        if (n.isTextual()) {
            if (!content.getScorers().containsKey(n.textValue())) {
                throw new IllegalArgumentException("Invalid scorer name " + n.textValue());
            }

            return content.getScorers().get(n.textValue());
        }

        String type = n.required("type").textValue();
        if (type.equals("value")) {
            return new ValueScorer(n.required("value").doubleValue());
        } else if (type.equals("chain")) {
            var operator = n.required("operator").textValue();
            if (!operator.equals("add") && !operator.equals("multiply") && !operator.equals("divide") && !operator.equals("subtract")) {
                throw new IllegalArgumentException("Invalid operator: " + operator);
            }
            var name = Optional.ofNullable(n.get("name")).map(JsonNode::textValue);
            return new ChainedScorer(operator, scorersFromJsonNode(n.required("scorers"), content),
                    name);
        } else if (type.equals("pathValue")) {
            return new PathValueScorer(
                    n.required("node").textValue(),
                    n.required("path").textValue(),
                    Optional.ofNullable(n.get("name")).map(JsonNode::textValue));
        } else if (type.equals("pathCount")) {
            return new PathCountScorer(
                    n.required("node").textValue(),
                    n.required("path").textValue(),
                    Optional.ofNullable(n.get("name")).map(JsonNode::textValue));
        } else if (type.equals("conditions")) {
            return new ConditionScorer(
                    n.required("value").doubleValue(),
                    AchievementCondition.parseConditionNode(n.required("conditions"), content),
                    Optional.ofNullable(n.get("name")).map(JsonNode::textValue));
        } else {
            throw new IllegalArgumentException("Invalid scorer type: " + type);
        }
    }

    double score(Map<String, Node> nodes, Map<AchievementVariable, String> vars);

    String toReadableString();

    Map<String, Double> getValues(Map<String, Node> nodes, Map<AchievementVariable, String> vars);

    class ValueScorer implements AchievementScorer {

        private double value;

        public ValueScorer(double value) {
            this.value = value;
        }

        @Override
        public double score(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            return value;
        }

        @Override
        public String toReadableString() {
            return String.valueOf(value);
        }

        @Override
        public Map<String, Double> getValues(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            return Map.of();
        }
    }

    class PathValueScorer implements AchievementScorer {

        private String node;
        private String path;
        private Optional<String> name;

        public PathValueScorer(String node, String path, Optional<String> name) {
            this.node = node;
            this.path = path;
            this.name = name;
        }

        @Override
        public double score(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            ArrayNode r = JsonPath.read(nodes.get(node), AchievementVariable.applyVariables(vars, path));
            if (r.getNodes().size() != 1) {
                throw new JsonPathException("Returned value for path " + path + " is not a single value");
            }

            Object value = ((ValueNode) r.getNodes().get(0)).getValue();
            return (value instanceof Long ? ((Long) value).doubleValue() : (double) value);
        }

        @Override
        public String toReadableString() {
            return "<" + name.orElseThrow(() -> new IllegalArgumentException("No name provided")) + ">";
        }

        @Override
        public Map<String, Double> getValues(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            double score = score(nodes, vars);

            LoggerFactory.getLogger(AchievementScorer.class).debug("Scoring filter count"
                    + "\n    name: " + name.orElse("none")
                    + "\n    node: " + node
                    + "\n    filter: " + path
                    + "\n    result: " + score);

            return name.map(s -> Map.of(s, score))
                    .orElseGet(Map::of);
        }
    }

    class PathCountScorer implements AchievementScorer {

        private String node;
        private String path;
        private Optional<String> name;

        public PathCountScorer(String node, String path, Optional<String> name) {
            this.node = node;
            this.path = path;
            this.name = name;
        }

        @Override
        public double score(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            ArrayNode r = JsonPath.read(nodes.get(node), AchievementVariable.applyVariables(vars, path));
            return r.getNodes().size();
        }

        @Override
        public String toReadableString() {
            return "<" + name.orElseThrow(() -> new IllegalArgumentException("No name provided")) + ">";
        }

        @Override
        public Map<String, Double> getValues(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            double score = score(nodes, vars);
            LoggerFactory.getLogger(AchievementScorer.class).debug("Scoring path count"
                    + "\n    name: " + name.orElse("none")
                    + "\n    node: " + node
                    + "\n    path: " + path
                    + "\n    result: " + score);
            return name.map(s -> Map.of(s, score)).orElseGet(Map::of);
        }
    }

    class ChainedScorer implements AchievementScorer {

        private Optional<String> name;
        private String type;
        private List<AchievementScorer> scorers;

        public ChainedScorer(String type, List<AchievementScorer> scorers, Optional<String> name) {
            this.name = name;
            this.type = type;
            this.scorers = scorers;
        }

        @Override
        public double score(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            double s = scorers.get(0).score(nodes, vars);
            boolean mult = type.equals("multiply") || type.equals("divide");
            if (mult) {
                for (int i = 1; i < scorers.size(); i++) {
                    s = type.equals("multiply") ? s * scorers.get(i).score(nodes, vars) : s / scorers.get(i).score(nodes, vars);
                }
            } else {
                for (int i = 1; i < scorers.size(); i++) {
                    s = type.equals("add") ? s + scorers.get(i).score(nodes, vars) : s - scorers.get(i).score(nodes, vars);
                }
            }
            return s;
        }

        @Override
        public String toReadableString() {
            if (name.isPresent()) {
                return "<" + name.orElseThrow(() -> new IllegalArgumentException("No name provided")) + ">";
            }

            String s = scorers.get(0).toReadableString();
            for (int i = 1; i < scorers.size(); i++) {
                s = s + (type.equals("multiply") ?
                        " * " : (type.equals("divide") ?
                        " / " : (type.equals("add") ?
                        " + " : " - ")))
                        + scorers.get(i).toReadableString();
            }
            return scorers.size() > 1 ? "(" + s + ")" : s;
        }

        @Override
        public Map<String, Double> getValues(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            Map<String, Double> values = new LinkedHashMap<>();
            name.ifPresent(s -> {
                double score = score(nodes, vars);
                values.put(s, score);
                LoggerFactory.getLogger(AchievementScorer.class).debug("Scoring chain"
                        + "\n    name: " + name.orElse("none")
                        + "\n    type: " + type
                        + "\n    result: " + score);
            });
            scorers.stream().forEach(s -> values.putAll(s.getValues(nodes, vars)));
            return values;
        }
    }

    class ConditionScorer implements AchievementScorer {

        private double value;
        private List<AchievementCondition> conditions;
        private Optional<String> name;

        public ConditionScorer(double value, List<AchievementCondition> conditions, Optional<String> name) {
            this.value = value;
            this.conditions = conditions;
            this.name = name;
        }

        @Override
        public double score(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            if (AchievementMatcher.checkConditions(nodes, vars, conditions).isFullfilled()) {
                return value;
            } else {
                return 0;
            }
        }

        @Override
        public String toReadableString() {
            return "<" + name.orElseThrow(() -> new IllegalArgumentException("No name provided")) + ">";
        }

        @Override
        public Map<String, Double> getValues(Map<String, Node> nodes, Map<AchievementVariable, String> vars) {
            double score = score(nodes, vars);
            LoggerFactory.getLogger(AchievementScorer.class).debug("Scoring condition "
                    + "\n    name: " + name.orElse("none")
                    + "\n    conditions: " + conditions.stream()
                    .map(AchievementCondition::getDescription)
                    .collect(Collectors.joining())
                    + "\n    result: " + score);
            return name.map(s -> Map.of(s, score)).orElseGet(Map::of);
        }
    }
}
