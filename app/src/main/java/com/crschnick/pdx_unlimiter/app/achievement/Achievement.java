package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Achievement {

    private static class Condition {
        private String description;
        private String node;
        private Filter filter;

        public Condition(String description, String node, String filter) {
            this.description = description;
            this.node = node;
            this.filter = Filter.parse("[?(" + filter + ")]");
        }
    }

    private static class Variable {
        private String name;
        private String value;

        public Variable(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    private String name;
    private String description;
    private UUID uuid;
    private Optional<String> icon;
    private List<Variable> variables;
    private List<Condition> eligibilityConditions;
    private List<Condition> achievementConditions;
    private Scorer scorer;

    public static Achievement fromFile(Path file) throws IOException {
        ObjectMapper o = new ObjectMapper();
        JsonNode node = o.readTree(Files.readAllBytes(file));

        Achievement a = new Achievement();
        JsonNode n = node.get("achievement");
        a.name = n.get("name").textValue();
        a.description = n.get("description").textValue();
        a.uuid = UUID.fromString(n.get("uuid").textValue());
        a.icon = Optional.ofNullable(n.get("icon")).map(JsonNode::textValue);
        a.variables = new ArrayList<>();

        Iterable<JsonNode> ec = () -> n.get("eligibilityConditions").elements();
        a.eligibilityConditions = StreamSupport.stream(ec.spliterator(), false)
                .map(ecn -> new Condition(
                        ecn.get("description").textValue(),
                        ecn.get("node").textValue(),
                        applyVariables(a.variables, ecn.get("filter").textValue())))
                .collect(Collectors.toList());

        Iterable<JsonNode> ac = () -> n.get("achievementConditions").elements();
        a.achievementConditions = StreamSupport.stream(ac.spliterator(), false)
                .map(acn -> new Condition(
                        acn.get("description").textValue(),
                        acn.get("node").textValue(),
                        applyVariables(a.variables, acn.get("filter").textValue())))
                .collect(Collectors.toList());


        a.scorer = Scorer.fromJsonNode(n.get("score"), s -> applyVariables(a.variables, s));
        return a;
    }

    private static String applyVariables(List<Variable> variables, String s) {
        String r = s;
        for (Variable v : variables) {
            r = r.replace("${" + v.name + "}", v.value);
        }
        return r;
    }

    public boolean isEligible(Eu4IntermediateSavegame s) {
        return check(s, eligibilityConditions);
    }

    public boolean isAchieved(Eu4IntermediateSavegame s) {
        return check(s, achievementConditions);
    }

    public double score(Eu4IntermediateSavegame s) {
        return scorer.score(s, 0);
    }

    private boolean check(Eu4IntermediateSavegame s, List<Condition> conditions) {
        for (var e : s.getNodes().entrySet()) {
            for (var condition : conditions) {
                if (condition.node.equals(e.getKey())) {
                    ArrayNode r = JsonPath.read(e.getValue(), "[?]", condition.filter);
                    if (r.getNodes().size() == 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String getReadableScore() {
        return scorer.toReadableString();
    }
}
