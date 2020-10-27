package com.crschnick.pdx_unlimiter.app.achievement;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AchievementCondition {

    public static List<AchievementCondition> parseConditionNode(JsonNode node, AchievementContent content) {
        return StreamSupport.stream(node.spliterator(), false)
                .map(acn -> acn.isTextual() ? content.getConditions().get(acn.textValue()) : new AchievementCondition(
                        acn.required("description").textValue(),
                        Optional.ofNullable(acn.get("node")).map(JsonNode::textValue),
                        acn.required("filter").textValue()))
                .collect(Collectors.toList());
    }

    private String description;
    private Optional<String> node;
    private String filter;

    public AchievementCondition(String description, String node, String filter) {
        this.description = description;
        this.node = Optional.of(node);
        this.filter = filter;
    }

    public AchievementCondition(String description, Optional<String> node, String filter) {
        this.description = description;
        this.node = node;
        this.filter = filter;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getNode() {
        return node;
    }

    public String getFilter() {
        return filter;
    }
}
