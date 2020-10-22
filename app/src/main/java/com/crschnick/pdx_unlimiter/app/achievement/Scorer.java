package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.util.*;
import java.util.function.Function;

public interface Scorer {

    static Scorer fromJsonNode(JsonNode node, Function<String,String> func) {
        List<Scorer> scorers = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> e = it.next();
            if (e.getKey().equals("add")) {
                scorers.add(new AddScorer(e.getValue().doubleValue()));
            }
            else if (e.getKey().equals("multiply")) {
                scorers.add(new MultScorer(e.getValue().doubleValue()));
            }
            else if (e.getKey().equals("chain")) {
                scorers.add(fromJsonNode(e.getValue(), func));
            }
            else if (e.getKey().equals("pathValue")) {
                scorers.add(new PathValueScorer(
                        e.getValue().get("node").textValue(),
                        func.apply(e.getValue().get("path").textValue()),
                        e.getValue().get("name").textValue()));
            }
            else if (e.getKey().equals("filterCount")) {
                scorers.add(new FilterCountScorer(
                        e.getValue().get("node").textValue(),
                        func.apply(e.getValue().get("filter").textValue()),
                        e.getValue().get("name").textValue()));
            } else {

                throw new IllegalArgumentException("Invalid scorer " + e.getKey());
            }

        }
        if (scorers.size() > 1) {
            return new ChainedScorer(scorers);
        } else {
            return scorers.get(0);
        }
    }

    double score(Eu4IntermediateSavegame sg, double current);

    String toReadableString();

    String getDelimiter();

    Map<String, Double> getValues(Eu4IntermediateSavegame sg);

    class AddScorer implements Scorer {

        private double value;

        public AddScorer(double value) {
            this.value = value;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, double current) {
            return current + value;
        }

        @Override
        public String toReadableString() {
            return String.valueOf(value >= 0 ? value : -value);
        }

        @Override
        public String getDelimiter() {
            return value >= 0 ? "+" : "-";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg) {
            return Map.of();
        }
    }

    class MultScorer implements Scorer {

        private double value;

        public MultScorer(double value) {
            this.value = value;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, double current) {
            return current * value;
        }

        @Override
        public String toReadableString() {
            return String.valueOf(value);
        }

        @Override
        public String getDelimiter() {
            return "*";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg) {
            return Map.of();
        }
    }

    class PathValueScorer implements Scorer {

        private String node;
        private String path;
        private String name;

        public PathValueScorer(String node, String path, String name) {
            this.node = node;
            this.path = path;
            this.name = name;
        }



        @Override
        public double score(Eu4IntermediateSavegame sg, double current) {
            return current + getValues(sg).get(name);
        }

        @Override
        public String toReadableString() {
            return "<" + name + ">";
        }

        @Override
        public String getDelimiter() {
            return "+";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), path);
            if (r.getNodes().size() > 1) {
                throw new JsonPathException();
            }

            Object value = ((ValueNode) r.getNodes().get(0)).getValue();
            return Map.of(name, (value instanceof Long ? ((Long)value).doubleValue() : (double) value));
        }
    }

    class FilterCountScorer implements Scorer {

        private String node;
        private String filter;
        private String name;

        public FilterCountScorer(String node, String filter, String name) {
            this.node = node;
            this.filter = filter;
            this.name = name;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, double current) {
            return current + getValues(sg).get(name);
        }

        @Override
        public String toReadableString() {
            return "<" + name + ">";
        }

        @Override
        public String getDelimiter() {
            return "+";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg) {
            ArrayNode r = JsonPath.read(sg.getNodes().get(node), filter);
            return Map.of(name, (double) r.getNodes().size());
        }
    }

    class ChainedScorer implements Scorer {

        private List<Scorer> scorers;

        public ChainedScorer(List<Scorer> scorers) {
            this.scorers = scorers;
        }

        @Override
        public double score(Eu4IntermediateSavegame sg, double current) {
            double s = 0;
            for (Scorer sc : scorers) {
                s = sc.score(sg, s);
            }
            return s;
        }

        @Override
        public String toReadableString() {
            if (scorers.size() == 1) {
                return scorers.get(0).toReadableString();
            }

            String start = scorers.get(0).toReadableString() + scorers.get(1).getDelimiter() + scorers.get(1).toReadableString();
            if (scorers.size() == 2) {
                return start;
            }

            String s = "(" + start + ")";
            for (int i = 2; i < scorers.size(); i++) {
                s = "(" + s + " " + scorers.get(i).getDelimiter() + " " + scorers.get(i).toReadableString() + ")";
            }
            return s;
        }

        @Override
        public String getDelimiter() {
            return "+";
        }

        @Override
        public Map<String, Double> getValues(Eu4IntermediateSavegame sg) {
            Map<String, Double> values = new LinkedHashMap<>();
            scorers.stream().forEach(s -> values.putAll(s.getValues(sg)));
            return values;
        }
    }
}
