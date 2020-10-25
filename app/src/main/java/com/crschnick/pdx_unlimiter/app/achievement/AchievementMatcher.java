package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.crschnick.pdx_unlimiter.eu4.parser.ArrayNode;
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
        Map<AchievementVariable, String> vars = a.evaluateVariables(s);
        vars.entrySet().stream().forEach(e -> LoggerFactory.getLogger(AchievementMatcher.class).debug(
                "variable: " + e.getKey().getName()
                        + ", expression: " + e.getKey().getExpression()
                        + ", result: " + e.getValue()));
        this.typeStatus = a.getTypes().stream()
                .collect(Collectors.toMap(t -> t, t -> check(s, t.getConditions(), a, vars)));
        this.eligibleStatus = check(s, a.getEligibilityConditions(), a, vars);
        this.achievementStatus = check(s, a.getAchievementConditions(), a, vars);
        this.status = score(s, a, vars);
    }

    private AchievementMatcher.ScoreStatus score(Eu4IntermediateSavegame s, Achievement a, Map<AchievementVariable, String> vars) {
        return new AchievementMatcher.ScoreStatus(
                a.getScorer().score(s, string -> a.applyVariables(vars, string)),
                a.getScorer().getValues(s, string -> a.applyVariables(vars, string)));
    }

    private AchievementMatcher.ConditionStatus check(Eu4IntermediateSavegame s, List<AchievementCondition> conditions, Achievement a, Map<AchievementVariable, String> vars) {
        AchievementMatcher.ConditionStatus status = new AchievementMatcher.ConditionStatus();
        for (var e : s.getNodes().entrySet()) {
            for (var condition : conditions) {
                if (condition.getNode().equals(e.getKey())) {
                    String p = a.applyVariables(vars, condition.getFilter());
                    try {
                        ArrayNode r = JsonPath.read(e.getValue(), p);
                        if (r.getNodes().size() == 0) {
                            status.add(condition, false);
                        } else {
                            status.add(condition, true);
                        }
                    } catch (Exception ex) {
                        throw new IllegalArgumentException("Exception while applying filter " + p, ex);
                    }
                }
            }
        }
        return status;
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
