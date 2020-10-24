package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.function.Function;

public interface Scorer {

    static Scorer fromJsonNode(JsonNode node) {
        List<Scorer> scorers = scorersFromJsonNode(node);
        if (scorers.size() == 1) {
            return new ChainedScorer(false, scorers);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static List<Scorer> scorersFromJsonNode(JsonNode node) {
        List<Scorer> scorers = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> e = it.next();
            if (e.getKey().equals("value")) {
                scorers.add(new ValueScorer(e.getValue().doubleValue()));
            }
            else if (e.getKey().equals("add")) {
                scorers.add(new ChainedScorer(false, scorersFromJsonNode(e.getValue()),
                        Optional.ofNullable(e.getValue().get("name").textValue())));
            }
            else if (e.getKey().equals("mult")) {
                scorers.add(new ChainedScorer(true, scorersFromJsonNode(e.getValue()),
                        Optional.ofNullable(e.getValue().get("name").textValue())));
            }
            else if (e.getKey().equals("pathValue")) {
                scorers.add(new PathValueScorer(
                        e.getValue().get("node").textValue(),
                        e.getValue().get("path").textValue(),
                        Optional.ofNullable(e.getValue().get("name").textValue())));
            }
            else if (e.getKey().equals("filterCount")) {
                scorers.add(new FilterCountScorer(
                        e.getValue().get("node").textValue(),
                        e.getValue().get("filter").textValue(),
                        Optional.ofNullable(e.getValue().get("name").textValue())));
            } else {

                throw new IllegalArgumentException("Invalid scorer " + e.getKey());
            }

        }
        return scorers;
    }

    double score(Eu4IntermediateSavegame sg, Function<String,String> func);

    String toReadableString();

    Map<String, Double> getValues(Eu4IntermediateSavegame sg, Function<String,String> func);

    class ValueScorer implements Scorer {

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

    class PathValueScorer implements Scorer {

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
            return getValues(sg, func).get(name);
        }

        @Override
        public String toReadableString() {
            return "<" + name + ">";
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

    class FilterCountScorer implements Scorer {

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
            return getValues(sg, func).get(name);
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

    class ChainedScorer implements Scorer {

        private Optional<String> name;
        private boolean mult;
        private List<Scorer> scorers;

        public ChainedScorer(boolean mult, List<Scorer> scorers, Optional<String> name) {
            this.name = name;
            this.mult = mult;
            this.scorers = scorers;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, Function<String,String> func) {
            double s = mult ? 1 : 0;
            for (Scorer sc : scorers) {
                if (mult) {
                    s *= sc.score(sg, func);
                } else {
                    s += sc.score(sg, func);
                }
            }
            return s;
        }

        @Override
        public String toReadableString() {
            String s = scorers.get(0).toReadableString();
            for (int i = 1; i < scorers.size(); i++) {
                s = s + (mult ? " * " : " + ") + scorers.get(i).toReadableString();
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
