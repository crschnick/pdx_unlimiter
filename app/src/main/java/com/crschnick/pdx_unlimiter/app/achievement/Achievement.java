package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.savegame_mgr.ErrorHandler;
import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
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

    public static class Matcher {

        private ConditionStatus eligibleStatus;
        private ConditionStatus achievementStatus;
        private ScoreStatus status;

        private Matcher(ConditionStatus eligibleStatus, ConditionStatus achievementStatus, ScoreStatus status) {
            this.eligibleStatus = eligibleStatus;
            this.achievementStatus = achievementStatus;
            this.status = status;
        }

        public ConditionStatus getEligibleStatus() {
            return eligibleStatus;
        }

        public ConditionStatus getAchievementStatus() {
            return achievementStatus;
        }

        public ScoreStatus getScoreStatus() {
            return status;
        }
    }

    public static class Condition {
        private String description;
        private String node;
        private String filter;

        public Condition(String description, String node, String filter) {
            this.description = description;
            this.node = node;
            this.filter = "[?(" + filter + ")]";
        }

        public String getDescription() {
            return description;
        }

        public String getNode() {
            return node;
        }

        public String  getFilter() {
            return filter;
        }
    }

    public static class ConditionStatus {
        private Map<Condition,Boolean> conditions = new LinkedHashMap<>();

        private void add(Condition c, boolean b) {
            conditions.put(c, b);
        }

        public Map<Condition, Boolean> getConditions() {
            return conditions;
        }

        public boolean isFullfilled() {
            return !conditions.containsValue(false);
        }
    }

    public static class ScoreStatus {
        private double score;
        private Map<String,Double> values;

        public ScoreStatus(double score, Map<String, Double> values) {
            this.score = score;
            this.values = values;
        }

        public double getScore() {
            return score;
        }

        public Map<String, Double> getValues() {
            return values;
        }
    }

    private String name;
    private String description;
    private UUID uuid;
    private Optional<Path> icon;
    private List<Variable> variables;
    private List<Condition> eligibilityConditions;
    private List<Condition> achievementConditions;
    private Scorer scorer;

    public static Achievement fromFile(Path file) throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        ObjectMapper o = new ObjectMapper(f);
        JsonNode node = o.readTree(Files.readAllBytes(file));

        Achievement a = new Achievement();
        JsonNode n = node.get("achievement");
        a.name = n.get("name").textValue();
        a.description = n.get("description").textValue();
        a.uuid = UUID.fromString(n.get("uuid").textValue());

        a.variables = Variable.defaultVariables();

        a.icon = Optional.ofNullable(n.get("icon"))
                .map(JsonNode::textValue)
                .map(s -> applyVariables(null, a.variables, s))
                .map(Path::of);

        Iterable<Map.Entry<String,JsonNode>> v = () -> n.get("variables").fields();
        a.variables.addAll(StreamSupport.stream(v.spliterator(), false)
                .map(vn -> Variable.fromNode(vn.getKey(), vn.getValue()))
                .collect(Collectors.toList()));

        Iterable<JsonNode> ec = () -> n.get("eligibilityConditions").elements();
        a.eligibilityConditions = StreamSupport.stream(ec.spliterator(), false)
                .map(ecn -> new Condition(
                        ecn.get("description").textValue(),
                        ecn.get("node").textValue(),
                        ecn.get("filter").textValue()))
                .collect(Collectors.toList());

        Iterable<JsonNode> ac = () -> n.get("achievementConditions").elements();
        a.achievementConditions = StreamSupport.stream(ac.spliterator(), false)
                .map(acn -> new Condition(
                        acn.get("description").textValue(),
                        acn.get("node").textValue(),
                        acn.get("filter").textValue()))
                .collect(Collectors.toList());


        a.scorer = Scorer.fromJsonNode(n.get("score"));
        return a;
    }

    private static String applyVariables(Eu4IntermediateSavegame sg, List<Variable> variables, String s) {
        String r = s;
        for (Variable v : variables) {
            r = r.replace("${" + v.getName() + "}", v.evaluate(sg));
        }
        return r;
    }

    public Matcher match(Eu4IntermediateSavegame sg) {
        return new Matcher(checkEligible(sg), checkAchieved(sg), score(sg));
    }

    private ConditionStatus checkEligible(Eu4IntermediateSavegame s) {
        return check(s, eligibilityConditions);
    }

    private ConditionStatus checkAchieved(Eu4IntermediateSavegame s) {
        return check(s, achievementConditions);
    }

    private ScoreStatus score(Eu4IntermediateSavegame s) {
        return new ScoreStatus(
                scorer.score(s, string -> applyVariables(s, variables, string)),
                scorer.getValues(s, string -> applyVariables(s, variables, string)));
    }

    private ConditionStatus check(Eu4IntermediateSavegame s, List<Condition> conditions) {
        ConditionStatus status = new ConditionStatus();
        for (var e : s.getNodes().entrySet()) {
            for (var condition : conditions) {
                if (condition.node.equals(e.getKey())) {
                    ArrayNode r = JsonPath.read(e.getValue(), applyVariables(s,variables, condition.filter));
                    if (r.getNodes().size() == 0) {
                        status.add(condition, false);
                    } else {
                        status.add(condition, true);
                    }
                }
            }
        }
        return status;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Optional<Path> getIcon() {
        return icon;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public List<Condition> getEligibilityConditions() {
        return eligibilityConditions;
    }

    public List<Condition> getAchievementConditions() {
        return achievementConditions;
    }

    public Scorer getScorer() {
        return scorer;
    }

    public String getReadableScore() {
        return scorer.toReadableString();
    }
}
