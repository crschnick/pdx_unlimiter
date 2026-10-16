package com.crschnick.pdxu.app.platform;

import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import lombok.Value;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@SuppressWarnings("InfiniteLoopStatement")
public class BindingsHelper {

    private static final Set<ReferenceEntry> REFERENCES = new HashSet<>();

    static {
        ThreadHelper.createPlatformThread("Binding Reference GC", true, () -> {
                    while (true) {
                        synchronized (REFERENCES) {
                            REFERENCES.removeIf(ReferenceEntry::canGc);
                        }
                        ThreadHelper.sleep(1000);

                        // Use for testing
                        // System.gc();
                    }
                })
                .start();
    }

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


    public static void preserve(Object source, Object target) {
        synchronized (REFERENCES) {
            REFERENCES.add(new ReferenceEntry(new WeakReference<>(source), target));
        }
    }

    public static <T, U> ObservableValue<U> map(
            ObservableValue<T> observableValue, Function<? super T, ? extends U> mapper) {
        return Bindings.createObjectBinding(
                () -> {
                    return mapper.apply(observableValue.getValue());
                },
                observableValue);
    }

    public static <T, U> ObservableValue<U> flatMap(
            ObservableValue<T> observableValue, Function<? super T, ? extends ObservableValue<? extends U>> mapper) {
        var prop = new SimpleObjectProperty<U>();
        Runnable runnable = () -> {
            prop.bind(mapper.apply(observableValue.getValue()));
        };
        runnable.run();
        observableValue.addListener((observable, oldValue, newValue) -> {
            runnable.run();
        });
        preserve(prop, observableValue);
        return prop;
    }

    @Value
    private static class ReferenceEntry {

        WeakReference<?> source;
        Object target;

        public boolean canGc() {
            return source.get() == null;
        }
    }
}
