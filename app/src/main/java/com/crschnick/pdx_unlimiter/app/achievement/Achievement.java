package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.Eu4IntermediateSavegame;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Achievement {

    private boolean official;
    private String name;
    private String description;
    private UUID uuid;
    private Optional<Path> icon;
    private List<AchievementVariable> variables;
    private List<Type> types;
    private List<AchievementCondition> eligibilityConditions;
    private List<AchievementCondition> achievementConditions;
    private AchievementScorer scorer;

    public static Achievement fromFile(Path file, AchievementContent content) throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        ObjectMapper o = new ObjectMapper(f);
        JsonNode node = o.readTree(Files.readAllBytes(file));

        Achievement a = new Achievement();
        JsonNode n = node.get("achievement");
        a.name = n.get("name").textValue();
        a.description = n.get("description").textValue();
        a.uuid = UUID.fromString(n.get("uuid").textValue());

        a.variables = new ArrayList<>(content.getVariables());

        a.icon = Optional.ofNullable(n.get("icon"))
                .map(JsonNode::textValue)
                .map(Path::of);

        Iterable<Map.Entry<String,JsonNode>> v = () -> n.get("variables").fields();
        a.variables.addAll(StreamSupport.stream(v.spliterator(), false)
                .map(vn -> AchievementVariable.fromNode(vn.getKey(), vn.getValue()))
                .collect(Collectors.toList()));

        a.eligibilityConditions = AchievementCondition.parseConditionNode(n.get("eligibilityConditions"), content);
        a.achievementConditions = AchievementCondition.parseConditionNode(n.get("achievementConditions"), content);
        a.types = StreamSupport.stream(n.get("types").spliterator(), false)
                .map(tn -> new Type(tn.get("name").textValue(), AchievementCondition.parseConditionNode(tn.get("conditions"), content)))
                .collect(Collectors.toList());

        a.scorer = AchievementScorer.fromJsonNode(n.get("score"), content);
        return a;
    }

    public Map<AchievementVariable,String> evaluateVariables(Eu4IntermediateSavegame sg) {
        Map<AchievementVariable,String> expr = new HashMap<>();
        for (int i = 0; i < variables.size(); i++) {
            AchievementVariable v = variables.get(i);
            String currentVar = v.getExpression();
            for (var e : expr.entrySet()) {
                currentVar = currentVar.replace("${" + e.getKey().getName() + "}", e.getValue());
            }
            expr.put(v, v.evaluate(sg, currentVar));
        }
        return expr;
    }

    public String applyVariables(Map<AchievementVariable,String> expr, String input) {
        String s = input;
        for (var e : expr.entrySet()) {
            s = s.replace("${" + e.getKey().getName() + "}", e.getValue());
        }
        return s;
    }

    public AchievementMatcher match(Eu4IntermediateSavegame sg) {
        return new AchievementMatcher(sg, this);
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

    public List<AchievementVariable> getVariables() {
        return variables;
    }

    public List<AchievementCondition> getEligibilityConditions() {
        return eligibilityConditions;
    }

    public List<AchievementCondition> getAchievementConditions() {
        return achievementConditions;
    }

    public AchievementScorer getScorer() {
        return scorer;
    }

    public String getReadableScore() {
        return scorer.toReadableString();
    }

    public List<Type> getTypes() {
        return types;
    }

    public static class Type {
        private String name;
        private List<AchievementCondition> conditions;

        public Type(String name, List<AchievementCondition> conditions) {
            this.name = name;
            this.conditions = conditions;
        }

        public String getName() {
            return name;
        }

        public List<AchievementCondition> getConditions() {
            return conditions;
        }
    }

    public boolean isOfficial() {
        return official;
    }
}
