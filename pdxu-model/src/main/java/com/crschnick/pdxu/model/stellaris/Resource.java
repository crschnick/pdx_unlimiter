package com.crschnick.pdxu.model.stellaris;

import com.crschnick.pdxu.io.node.Node;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Optional;

@Jacksonized
@Builder
@Value
public class Resource {
    double stored;
    double income;
    double expense;

    public static Optional<Resource> parseFromCountryNode(Node node, String name) {
        var budget = node.getNodeForKeysIfExistent("budget", "current_month");
        if (budget.isEmpty()) {
            return Optional.empty();
        }

        final double[] income = {0.0};
        final double[] expense = {0.0};
        budget.get().getNodeForKeyIfExistent("income").ifPresent(incomeNode -> incomeNode.forEach((k, v) -> {
            v.forEach((resourceName, amount) -> {
                if (name.equals(resourceName)) {
                    income[0] += amount.getDouble();
                }
            });
        }));
        budget.get().getNodeForKeyIfExistent("expenses").ifPresent(expensesNode -> expensesNode.forEach((k, v) -> {
            v.forEach((resourceName, amount) -> {
                if (name.equals(resourceName)) {
                    expense[0] += amount.getDouble();
                }
            });
        }));
        var stored = node.getNodeForKeysIfExistent("modules", "standard_economy_module", "resources", name).map(Node::getDouble).orElse(0.0);
        return Optional.ofNullable(Resource.builder().stored(stored).income(income[0]).expense(expense[0]).build());
    }
}
