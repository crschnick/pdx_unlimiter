package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
import com.crschnick.pdx_unlimiter.eu4.parser.KeyValueNode;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
import com.crschnick.pdx_unlimiter.eu4.parser.ValueNode;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AchievementMatcher {

    private Map<Achievement.Type, ConditionStatus> typeStatus;
    private ConditionStatus eligibleStatus;
    private ConditionStatus achievementStatus;
    private ScoreStatus status;

    AchievementMatcher(Map<String,Node> nodes, Achievement a) {
        LoggerFactory.getLogger(Achievement.class).debug("Evaluating variables for achievement " + a.getName());
        Map<AchievementVariable, String> vars = AchievementVariable.evaluateVariables(a.getVariables(), nodes);
        this.typeStatus = a.getTypes().stream()
                .collect(Collectors.toMap(t -> t, t -> {
                    LoggerFactory.getLogger(Achievement.class).debug(
                            "Checking type " + t.getName() + " for achievement " + a.getName());
                    return checkConditions(nodes, vars, t.getConditions());
                }));

        LoggerFactory.getLogger(Achievement.class).debug("Checking eligibility for achievement " + a.getName());
        this.eligibleStatus = checkConditions(nodes, vars, a.getEligibilityConditions());
        LoggerFactory.getLogger(Achievement.class).debug("Checking achievement for achievement " + a.getName());
        this.achievementStatus = checkConditions(nodes, vars, a.getAchievementConditions());
        LoggerFactory.getLogger(Achievement.class).debug("Calculating score for achievement " + a.getName());
        this.status = score(nodes, a, vars);
    }

    private AchievementMatcher.ScoreStatus score(Map<String, Node> nodes, Achievement a, Map<AchievementVariable, String> vars) {
        return new AchievementMatcher.ScoreStatus(
                a.getScorer().score(nodes, vars),
                a.getScorer().getValues(nodes, vars));
    }

    public static AchievementMatcher.ConditionStatus checkConditions(Map<String,Node> nodes,
                                                                     Map<AchievementVariable, String> vars,
                                                                     List<AchievementCondition> conditions) {
        AchievementMatcher.ConditionStatus status = new AchievementMatcher.ConditionStatus();
        for (var condition : conditions) {
            String p = AchievementVariable.applyVariables(vars, condition.getFilter());

            if (condition.getNode().isEmpty()) {
                ArrayNode dummy = new ArrayNode();
                dummy.getNodes().add(KeyValueNode.create("value", new ValueNode(true)));
                status.add(condition, match(dummy, condition, p));
                continue;
            }

            for (var e : nodes.entrySet()) {
                if (condition.getNode().get().equals(e.getKey())) {
                    try {
                        status.add(condition, match(e.getValue(), condition, p));
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Exception while applying filter " + p, ex);
                    }
                }
            }
        }
        return status;
    }

    private static boolean match(Node input, AchievementCondition condition, String p) {
        ArrayNode r = JsonPath.read(input, p);
        LoggerFactory.getLogger(AchievementMatcher.class).debug(
                "Applying achievement condition"
                        + "\n    condition: " + condition.getDescription()
                        + "\n    node: " + condition.getNode().orElse("none")
                        + "\n    expression: " + condition.getFilter()
                        + "\n    evaluation: " + p
                        + "\n    result: " + (r.getNodes().size() > 0));
        return r.getNodes().size() > 0;
    }

    public Map<Achievement.Type, ConditionStatus> getTypeStatus() {
        return typeStatus;
    }

    public Optional<Achievement.Type> getValidType() {
        return typeStatus.entrySet().stream()
                .filter(e -> e.getValue().isFullfilled())
                .map(Map.Entry::getKey).findFirst();
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

    public static class ConditionStatus {
        private Map<AchievementCondition, Boolean> conditions = new LinkedHashMap<>();

        private void add(AchievementCondition c, boolean b) {
            conditions.put(c, b);
        }

        public Map<AchievementCondition, Boolean> getConditions() {
            return conditions;
        }

        public boolean isFullfilled() {
            return !conditions.containsValue(false);
        }
    }

    public static class ScoreStatus {
        private double score;
        private Map<String, Double> values;

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
}
