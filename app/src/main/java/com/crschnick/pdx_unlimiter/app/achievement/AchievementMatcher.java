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

    AchievementMatcher(Eu4IntermediateSavegame s, Achievement a) {
        Map<String, String> vars = a.evaluateVariables(s);
        this.typeStatus = a.getTypes().stream()
                .collect(Collectors.toMap(t -> t, t -> {
                    LoggerFactory.getLogger(Achievement.class).debug(
                            "Checking type " + t.getName() + " for achievement " + a.getName());
                    return check(s, t.getConditions(), a, vars);
                }));

        LoggerFactory.getLogger(Achievement.class).debug("Checking eligibility for achievement " + a.getName());
        this.eligibleStatus = check(s, a.getEligibilityConditions(), a, vars);
        LoggerFactory.getLogger(Achievement.class).debug("Checking achievement for achievement " + a.getName());
        this.achievementStatus = check(s, a.getAchievementConditions(), a, vars);
        LoggerFactory.getLogger(Achievement.class).debug("Calculating score for achievement " + a.getName());
        this.status = score(s, a, vars);
    }

    private AchievementMatcher.ScoreStatus score(Eu4IntermediateSavegame s, Achievement a, Map<String, String> vars) {
        return new AchievementMatcher.ScoreStatus(
                a.getScorer().score(s, string -> a.applyVariables(vars, string)),
                a.getScorer().getValues(s, string -> a.applyVariables(vars, string)));
    }

    private AchievementMatcher.ConditionStatus check(
            Eu4IntermediateSavegame s,
            List<AchievementCondition> conditions,
            Achievement a,
            Map<String, String> vars) {
        AchievementMatcher.ConditionStatus status = new AchievementMatcher.ConditionStatus();
        for (var condition : conditions) {
            String p = a.applyVariables(vars, condition.getFilter());

            if (condition.getNode().isEmpty()) {
                ArrayNode dummy = new ArrayNode();
                dummy.getNodes().add(KeyValueNode.create("value", new ValueNode(true)));
                status.add(condition, match(dummy, condition, p));
                continue;
            }

            for (var e : s.getNodes().entrySet()) {
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

    private boolean match(Node input, AchievementCondition condition, String p) {
        ArrayNode r = JsonPath.read(input, p);
        LoggerFactory.getLogger(AchievementMatcher.class).debug(
                "Applying achievement condition"
                        + "\n    condition: " + condition.getDescription()
                        + "\n    node: " + condition.getNode().orElse("none")
                        + "\n    expression: " + condition.getFilter()
                        + "\n    filter: " + p
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
