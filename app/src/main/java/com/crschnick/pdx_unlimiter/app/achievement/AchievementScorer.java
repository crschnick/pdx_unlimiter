package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.util.*;
import java.util.function.Function;

public interface AchievementScorer {

    static AchievementScorer fromJsonNode(JsonNode node,  AchievementContent content) {
        List<AchievementScorer> scorers = scorersFromJsonNode(node, content);
        if (scorers.size() == 1) {
            return scorers.get(0);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static List<AchievementScorer> scorersFromJsonNode(JsonNode node,  AchievementContent content) {
        List<AchievementScorer> scorers = new ArrayList<>();
        node.elements().forEachRemaining(n -> {
            if (n.isTextual()) {
                scorers.add(content.getScorers().get(n.textValue()));
                return;
            }

            String type = n.get("type").textValue();
            if (type.equals("value")) {
                scorers.add(new ValueScorer(n.get("value").doubleValue()));
            }
            else if (type.equals("add") || type.equals("subtract") || type.equals("mult") || type.equals("divide")) {
                var name = Optional.ofNullable(n.get("name")).map(JsonNode::textValue);
                scorers.add(new ChainedScorer(type, scorersFromJsonNode(n.get("scorers"), content),
                        name));
            }
            else if (type.equals("pathValue")) {
                scorers.add(new PathValueScorer(
                        n.get("node").textValue(),
                        n.get("path").textValue(),
                        Optional.ofNullable(n.get("name")).map(JsonNode::textValue)));
            }
            else if (type.equals("filterCount")) {
                scorers.add(new FilterCountScorer(
                        n.get("node").textValue(),
                        n.get("filter").textValue(),
                        Optional.ofNullable(n.get("name")).map(JsonNode::textValue)));
            } else {
                throw new IllegalArgumentException("Invalid scorer type: " + type);
            }

        });
        return scorers;
    }

    double score(Eu4IntermediateSavegame sg, Function<String,String> func);

    String toReadableString();

    Map<String, Double> getValues(Eu4IntermediateSavegame sg, Function<String,String> func);

    class ValueScorer implements AchievementScorer {

        private double value;

        public ValueScorer(double value) {
            this.value = value;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, Function<String,String> func) {
            return value;
        }

        @Override
        public String toReadableString() {
            return String.valueOf(value);
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg, Function<String,String> func) {
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
        public double score(Eu4IntermediateSavegame sg, Function<String,String> func) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), func.apply(path));
            if (r.getNodes().size() > 1) {
                throw new JsonPathException();
            }

            Object value = ((ValueNode) r.getNodes().get(0)).getValue();

            return (value instanceof Long ? ((Long) value).doubleValue() : (double) value);
        }

        @Override
        public String toReadableString() {
            return "<" + name.orElseThrow(IllegalArgumentException::new) + ">";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg, Function<String,String> func) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), func.apply(path));
            if (r.getNodes().size() > 1) {
                throw new JsonPathException();
            }

            Object value = ((ValueNode) r.getNodes().get(0)).getValue();
            return name.map(s -> Map.of(s, (value instanceof Long ? ((Long) value).doubleValue() : (double) value)))
                    .orElseGet(Map::of);
        }
    }

    class FilterCountScorer implements AchievementScorer {

        private String node;
        private String filter;
        private Optional<String> name;

        public FilterCountScorer(String node, String filter, Optional<String> name) {
            this.node = node;
            this.filter = filter;
            this.name = name;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, Function<String,String> func) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), func.apply(filter));
            return (double) r.getNodes().size();
        }

        @Override
        public String toReadableString() {
            return "<" + name + ">";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg, Function<String,String> func) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), func.apply(filter));
            return name.map(s -> Map.of(s, (double) r.getNodes().size())).orElseGet(Map::of);
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
        public double score(Eu4IntermediateSavegame sg, Function<String,String> func) {
            double s = scorers.get(0).score(sg, func);
            boolean mult = type.equals("multiply") || type.equals("divide");
            if (mult) {
                for (int i = 1; i < scorers.size(); i++) {
                    s = type.equals("multiply") ? s * scorers.get(i).score(sg, func) : s / scorers.get(i).score(sg, func);
                }
            } else {
                for (int i = 1; i < scorers.size(); i++) {
                    s = type.equals("add") ? s + scorers.get(i).score(sg, func) : s - scorers.get(i).score(sg, func);
                }
            }
            return s;
        }

        @Override
        public String toReadableString() {
            if (name.isPresent()) {
                return "<" + name.get() + ">";
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
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg, Function<String,String> func) {
            Map<String, Double> values = new LinkedHashMap<>();
            if (name.isPresent()) {
                values.put(name.get(), score(sg, func));
            } else {
                scorers.stream().forEach(s -> values.putAll(s.getValues(sg, func)));
            }
            return values;
        }
    }
}
