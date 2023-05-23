package com.crschnick.pdxu.model.stellaris;

import com.crschnick.pdxu.io.node.Node;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Value
public class Resource {
    double stored;
    double income;
    double expense;

    public static Resource parseFromCountryNode(Node node, String name) {
        var budget = node.getNodeForKeys("budget", "current_month");
        final double[] income = {0.0};
        final double[] expense = {0.0};
        budget.getNodeForKey("income").forEach((k, v) -> {
            v.forEach((resourceName, amount) -> {
                if (name.equals(resourceName)) {
                    income[0] += amount.getDouble();
                }
            });
        });
        budget.getNodeForKey("expenses").forEach((k, v) -> {
            v.forEach((resourceName, amount) -> {
                if (name.equals(resourceName)) {
                    expense[0] += amount.getDouble();
                }
            });
        });
        var stored = node.getNodeForKeys("modules", "standard_economy_module", "resources", name).getDouble();
        return Resource.builder().stored(stored).income(income[0]).expense(expense[0]).build();
    }
}
