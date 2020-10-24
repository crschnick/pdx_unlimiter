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

    private String name;
    private String description;
    private UUID uuid;
    private Optional<Path> icon;
    private List<Variable> variables;
    private List<Type> types;
    private List<AchievementCondition> eligibilityConditions;
    private List<AchievementCondition> achievementConditions;
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
                .map(s -> a.applyVariables(null, s))
                .map(Path::of);

        Iterable<Map.Entry<String,JsonNode>> v = () -> n.get("variables").fields();
        a.variables.addAll(StreamSupport.stream(v.spliterator(), false)
                .map(vn -> Variable.fromNode(vn.getKey(), vn.getValue()))
                .collect(Collectors.toList()));

        a.eligibilityConditions = AchievementCondition.parseConditionNode(n.get("eligibilityConditions"));
        a.achievementConditions = AchievementCondition.parseConditionNode(n.get("achievementConditions"));
        a.types = StreamSupport.stream(n.get("types").spliterator(), false)
                .map(tn -> new Type(tn.get("name").textValue(), AchievementCondition.parseConditionNode(tn.get("conditions"))))
                .collect(Collectors.toList());

        a.scorer = Scorer.fromJsonNode(n.get("score"));
        return a;
    }

    public String applyVariables(Eu4IntermediateSavegame sg, String s) {
        String r = s;
        for (Variable v : variables) {
            r = r.replace("${" + v.getName() + "}", v.evaluate(sg));
        }
        return r;
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

    public List<Variable> getVariables() {
        return variables;
    }

    public List<AchievementCondition> getEligibilityConditions() {
        return eligibilityConditions;
    }

    public List<AchievementCondition> getAchievementConditions() {
        return achievementConditions;
    }

    public Scorer getScorer() {
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

}
