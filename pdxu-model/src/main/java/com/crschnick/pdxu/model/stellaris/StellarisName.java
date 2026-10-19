package com.crschnick.pdxu.model.stellaris;

import com.crschnick.pdxu.io.node.Node;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class StellarisName {

    private String key;
    private boolean literal;
    private List<Variable> variables;

    public StellarisName() {}

    public static Optional<StellarisName> fromNode(Node node) {
        if (node == null || !node.isArray()) {
            return Optional.empty();
        }

        var key = node.getNodeForKeyIfExistent("key").filter(Node::isValue).map(Node::getString);
        if (key.isEmpty()) {
            return Optional.empty();
        }

        var name = new StellarisName();
        name.key = key.get();
        name.literal = node.getNodeForKeyIfExistent("literal")
                .filter(Node::isValue)
                .map(Node::getBoolean)
                .orElse(false);
        name.variables = new ArrayList<>();
        node.getNodeForKeyIfExistent("variables").filter(Node::isArray).ifPresent(vars -> {
            for (var v : vars.getNodeArray()) {
                if (!v.isArray()) {
                    continue;
                }

                var varKey = v.getNodeForKeyIfExistent("key").filter(Node::isValue).map(Node::getString);
                var value = v.getNodeForKeyIfExistent("value").flatMap(StellarisName::fromNode);
                if (varKey.isPresent() && value.isPresent()) {
                    name.variables.add(new Variable(varKey.get(), value.get()));
                }
            }
        });
        return Optional.of(name);
    }

    @Getter
    public static class Variable {

        private String key;
        private StellarisName value;

        public Variable() {}

        public Variable(String key, StellarisName value) {
            this.key = key;
            this.value = value;
        }
    }
}
