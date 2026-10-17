package com.crschnick.pdxu.app.platform;

import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BindingsHelper {

    public static <T> void setContent(ObservableList<T> list, List<? extends T> newList) {
        if (list.equals(newList)) {
            return;
        }

        if (list.isEmpty() || newList.isEmpty()) {
            list.setAll(newList);
            return;
        }

        var newSet = new HashSet<>(newList);
        var listSet = new HashSet<>(list);

        // Only additions
        if (newSet.containsAll(listSet)) {
            var retained = new ArrayList<T>(newList);
            retained.removeIf(t -> !listSet.contains(t));
            // Reordering occurred
            if (!retained.equals(list)) {
                list.setAll(newList);
                return;
            }

            for (int i = 0; i < newList.size(); i++) {
                if (!listSet.contains(newList.get(i))) {
                    list.add(i, newList.get(i));
                }
            }
            return;
        }

        // Only removals
        if (listSet.containsAll(newSet)) {
            list.removeIf(t -> !newSet.contains(t));
            // Reordering occurred
            if (!list.equals(newList)) {
                list.setAll(newList);
            }
            return;
        }

        list.setAll(newList);
    }

    // Keeps the target alive for as long as the node is alive
    // The node properties are released together with the node, so no manual cleanup is needed
    @SuppressWarnings("unchecked")
    public static void preserve(Node source, Object target) {
        var list = (List<Object>)
                source.getProperties().computeIfAbsent("bindingsHelperPreserved", k -> new ArrayList<>());
        list.add(target);
    }
}
