package com.crschnick.pdxu.io.node;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Map;

public class NodeEvaluator {

    private static final Context JAVASCRIPT_CONTEXT = Context.create("js");
    private static final NumberFormat FORMATTER = new DecimalFormat("#0.0000000");

    public static void evaluateArrayNode(ArrayNode arrayNode) {
        evaluateArrayNode(arrayNode, new NodeEnvironment(null, Map.of()));
    }

    public static void evaluateArrayNode(ArrayNode arrayNode, NodeEnvironment environment) {
        final NodeEnvironment[] currentEnvironment = {environment};
        arrayNode.forEach(
                (s, node) -> {
                    if (node.isValue()) {
                        var evaluated = evaluateValueNode(node.getValueNode(), currentEnvironment[0]);
                        if (evaluated.isValue() && evaluated != node) {
                            node.getValueNode().set(evaluated.getValueNode());
                        }
                        if (s != null && s.startsWith("@")) {
                            currentEnvironment[0].put(s.substring(1), evaluated);
                        }
                    } else if (node.isArray()) {
                        currentEnvironment[0] = currentEnvironment[0].copy(null);
                        evaluateArrayNode(node.getArrayNode(), currentEnvironment[0]);
                    }
                },
                true
        );
    }

    public static Node evaluateValueNode(ValueNode node, NodeEnvironment environment) {
        var expression = node.getInlineMathExpression();
        if (expression.isPresent()) {
            var string = expression.get();
            // Prevent cases of replacing a variable which is a sub string of another
            // by ordering them by length descending
            for (Map.Entry<String, Node> entry : environment.getVariables()
                    .entrySet()
                    .stream()
                    .sorted(Comparator.<Map.Entry<String, Node>>comparingInt(e -> e.getKey().length()).reversed())
                    .toList()) {
                if (!entry.getValue().isValue()) {
                    continue;
                }

                string = string.replaceAll(entry.getKey(), entry.getValue().getValueNode().getString());
            }

            Value eval = JAVASCRIPT_CONTEXT.eval("js", string);
            double result = eval.asDouble();
            return new ValueNode(FORMATTER.format(result), false);
        } else if (node.getString().startsWith("@")) {
            if (!environment.getVariables().containsKey(node.getString().substring(1))) {
                throw new IllegalArgumentException("Unresolved variable: " + node.getString());
            }

            return environment.getVariables().getOrDefault(node.getString().substring(1), node);
        }

        return node;
    }
}
