package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.eu4.savegame.Eu4Savegame;
import com.crschnick.pdx_unlimiter.eu4.parser.Node;
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
    private AchievementContent content;

    public static Achievement fromFile(Path file, AchievementContent content, boolean official) throws IOException {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        ObjectMapper o = new ObjectMapper(f);
        JsonNode node = o.readTree(Files.readAllBytes(file));

        Achievement a = new Achievement();
        JsonNode n = node.required("achievement");
        a.official = official;
        a.name = n.required("name").textValue();
        a.description = n.required("description").textValue();
        a.uuid = UUID.fromString(n.required("uuid").textValue());
        a.content = content;

        a.variables = new ArrayList<>(content.getPathVariables());

        a.icon = Optional.ofNullable(n.get("icon"))
                .map(JsonNode::textValue)
                .map(s -> AchievementVariable.applyVariables(AchievementVariable.evaluateVariables(a.variables, null), s))
                .map(Path::of);

        a.icon.ifPresent(i -> {
            if (!Files.exists(i)) {
                throw new IllegalArgumentException("Achievement image " + i.toString() + " does not exist");
            }
        });

        a.variables = new ArrayList<>(content.getVariables());

        Iterable<Map.Entry<String, JsonNode>> v = () -> n.required("variables").fields();
        a.variables.addAll(StreamSupport.stream(v.spliterator(), false)
                .map(vn -> AchievementVariable.fromNode(vn.getKey(), vn.getValue()))
                .collect(Collectors.toList()));

        a.eligibilityConditions = AchievementCondition.parseConditionNode(n.required("eligibilityConditions"), content);
        a.achievementConditions = AchievementCondition.parseConditionNode(n.required("achievementConditions"), content);
        a.types = StreamSupport.stream(n.required("types").spliterator(), false)
                .map(tn -> new Type(tn.required("name").textValue(), AchievementCondition.parseConditionNode(tn.required("conditions"), content)))
                .collect(Collectors.toList());

        a.scorer = AchievementScorer.fromJsonNode(n.required("score"), content);
        a.official = true;

        return a;
    }

    public AchievementMatcher match(Eu4Savegame sg) {
        Map<String, Node> nodes = new HashMap<>();
        nodes.putAll(sg.getNodes());
        nodes.putAll(content.getNodes());
        return new AchievementMatcher(nodes, this);
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

    public boolean isOfficial() {
        return official;
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
