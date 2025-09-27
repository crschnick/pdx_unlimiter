package com.crschnick.pdxu.io.node;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Map;

public class NodeEvaluator {

    private static Context JAVASCRIPT_CONTEXT;
    private static final NumberFormat FORMATTER = new DecimalFormat("#0.0000000");

    public static void evaluateArrayNode(ArrayNode arrayNode) {
        evaluateArrayNode(arrayNode, new NodeEnvironment(Map.of()));
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
                        currentEnvironment[0] = currentEnvironment[0].copy();
                        evaluateArrayNode(node.getArrayNode(), currentEnvironment[0]);
                    }
                },
                true
        );
    }

    public static Node evaluateValueNode(ValueNode node, NodeEnvironment environment) {
        if (ModuleLayer.boot().findModule("org.graalvm.js").isEmpty()) {
            throw new UnsupportedOperationException("Node evaluation is only supported with module org.graalvm.js in module path");
        }

        if (JAVASCRIPT_CONTEXT == null) {
            JAVASCRIPT_CONTEXT = Context.newBuilder("js").option("engine.WarnInterpreterOnly", "false").build();
        }

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

            try {
                Value eval = JAVASCRIPT_CONTEXT.eval("js", string);
                double result = eval.asDouble();
                return new ValueNode(FORMATTER.format(result), false);
            } catch (Throwable t) {
                return new ValueNode("0.0", false);
            }
        } else if (node.getString().startsWith("@")) {
            if (!environment.getVariables().containsKey(node.getString().substring(1))) {
                throw new IllegalArgumentException("Unresolved variable: " + node.getString());
            }

            return environment.getVariables().getOrDefault(node.getString().substring(1), node);
        }

        return node;
    }
}
