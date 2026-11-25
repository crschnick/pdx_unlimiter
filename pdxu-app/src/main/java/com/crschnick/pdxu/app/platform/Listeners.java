package com.crschnick.pdxu.app.platform;

import javafx.beans.WeakListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.Region;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Listeners {

    private static final class WeakRefChangeListener<V, T> implements ChangeListener<T>, WeakListener {

        private final WeakReference<V> ref;
        private final BiConsumer<V, T> consumer;

        private WeakRefChangeListener(V ref, BiConsumer<V, T> consumer) {
            this.ref = new WeakReference<>(ref);
            this.consumer = consumer;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            var v = ref.get();
            if (v != null) {
                consumer.accept(v, newValue);
            } else {
                observable.removeListener(this);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return ref.get() == null;
        }
    }

    @FunctionalInterface
    public interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }

    private static final class WeakRef2ChangeListener<V1, V2, T> implements ChangeListener<T>, WeakListener {

        private final WeakReference<V1> ref1;
        private final WeakReference<V2> ref2;
        private final TriConsumer<V1, V2, T> consumer;

        private WeakRef2ChangeListener(V1 ref1, V2 ref2, TriConsumer<V1, V2, T> consumer) {
            this.ref1 = new WeakReference<>(ref1);
            this.ref2 = new WeakReference<>(ref2);
            this.consumer = consumer;
        }

        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            var v1 = ref1.get();
            var v2 = ref2.get();
            if (v1 != null && v2 != null) {
                consumer.accept(v1, v2, newValue);
            } else {
                observable.removeListener(this);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return ref1.get() == null || ref2.get() == null;
        }
    }

    private static final class WeakRefListChangeListener<V, T> implements ListChangeListener<T>, WeakListener {

        private final WeakReference<V> ref;
        private final ObservableList<T> list;
        private final BiConsumer<V, List<T>> consumer;

        private WeakRefListChangeListener(V ref, ObservableList<T> list, BiConsumer<V, List<T>> consumer) {
            this.ref = new WeakReference<>(ref);
            this.list = list;
            this.consumer = consumer;
        }

        @Override
        public void onChanged(Change<? extends T> c) {
            var v = ref.get();
            if (v != null) {
                consumer.accept(v, list);
            } else {
                list.removeListener(this);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return ref.get() == null;
        }
    }

    private static final class WeakRef2ListChangeListener<V1, V2, T> implements ListChangeListener<T>, WeakListener {

        private final WeakReference<V1> ref1;
        private final WeakReference<V2> ref2;
        private final ObservableList<T> list;
        private final TriConsumer<V1, V2, List<T>> consumer;

        private WeakRef2ListChangeListener(
                V1 ref1, V2 ref2, ObservableList<T> list, TriConsumer<V1, V2, List<T>> consumer) {
            this.ref1 = new WeakReference<>(ref1);
            this.ref2 = new WeakReference<>(ref2);
            this.list = list;
            this.consumer = consumer;
        }

        @Override
        public void onChanged(Change<? extends T> c) {
            var v1 = ref1.get();
            var v2 = ref2.get();
            if (v1 != null && v2 != null) {
                consumer.accept(v1, v2, list);
            } else {
                list.removeListener(this);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return ref1.get() == null || ref2.get() == null;
        }
    }

    private static final class WeakRefSetChangeListener<V, T> implements SetChangeListener<T>, WeakListener {

        private final WeakReference<V> ref;
        private final ObservableSet<T> set;
        private final BiConsumer<V, Set<T>> consumer;

        private WeakRefSetChangeListener(V ref, ObservableSet<T> set, BiConsumer<V, Set<T>> consumer) {
            this.ref = new WeakReference<>(ref);
            this.set = set;
            this.consumer = consumer;
        }

        @Override
        public void onChanged(Change<? extends T> c) {
            var v = ref.get();
            if (v != null) {
                consumer.accept(v, set);
            } else {
                set.removeListener(this);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return ref.get() == null;
        }
    }

    public static <V, T> void listenSetWeak(V ref, ObservableSet<T> s, BiConsumer<V, Set<T>> consumer) {
        s.addListener(new WeakRefSetChangeListener<>(ref, s, consumer));
    }

    public static <V, T> void listenWeak(V ref, ObservableValue<T> value, BiConsumer<V, T> consumer) {
        value.addListener(new WeakRefChangeListener<>(ref, consumer));
    }

    public static <V, T> void subscribeWeak(V ref, ObservableValue<T> value, BiConsumer<V, T> consumer) {
        listenWeak(ref, value, consumer);
        consumer.accept(ref, value.getValue());
    }

    public static <V1, V2, T> void listenWeak(
            V1 ref1, V2 ref2, ObservableValue<T> value, TriConsumer<V1, V2, T> consumer) {
        value.addListener(new WeakRef2ChangeListener<>(ref1, ref2, consumer));
    }

    public static <V1, V2, T> void subscribeWeak(
            V1 ref1, V2 ref2, ObservableValue<T> value, TriConsumer<V1, V2, T> consumer) {
        listenWeak(ref1, ref2, value, consumer);
        consumer.accept(ref1, ref2, value.getValue());
    }

    public static <T> void subscribeList(ObservableList<T> l, Runnable r) {
        subscribeList(l, ignored -> r.run());
    }

    @SuppressWarnings("unchecked")
    public static <T> void subscribeList(ObservableList<T> l, Consumer<List<T>> r) {
        r.accept(l);
        l.addListener((ListChangeListener<? super T>) c -> {
            r.accept((List<T>) c.getList());
        });
    }

    public static <V, T> void listenListWeak(V ref, ObservableList<T> l, BiConsumer<V, List<T>> consumer) {
        l.addListener(new WeakRefListChangeListener<>(ref, l, consumer));
    }

    public static <V, T> void subscribeListWeak(V ref, ObservableList<T> l, BiConsumer<V, List<T>> consumer) {
        listenListWeak(ref, l, consumer);
        consumer.accept(ref, l);
    }

    public static <V1, V2, T> void listenListWeak(
            V1 ref1, V2 ref2, ObservableList<T> l, TriConsumer<V1, V2, List<T>> consumer) {
        l.addListener(new WeakRef2ListChangeListener<>(ref1, ref2, l, consumer));
    }

    public static <V1, V2, T> void subscribeListWeak(
            V1 ref1, V2 ref2, ObservableList<T> l, TriConsumer<V1, V2, List<T>> consumer) {
        listenListWeak(ref1, ref2, l, consumer);
        consumer.accept(ref1, ref2, l);
    }

    public static <T> void listenList(ObservableList<T> l, Runnable r) {
        listenList(l, ignored -> r.run());
    }

    @SuppressWarnings("unchecked")
    public static <T> void listenList(ObservableList<T> l, Consumer<List<T>> r) {
        l.addListener((ListChangeListener<? super T>) c -> {
            r.accept((List<T>) c.getList());
        });
    }

    public static <R extends Region, T> void attachWithScene(R node, ObservableValue<T> value, Consumer<T> consumer) {
        attachWithScene(node, value, consumer, false);
    }

    private static <R extends Region, T> void attachWithScene(
            R node, ObservableValue<T> value, Consumer<T> consumer, boolean applyInitially) {
        var lastScene = new AtomicReference<WeakReference<Scene>>();
        var lastValue = new AtomicReference<T>();
        Consumer<T> recordingConsumer = v -> {
            lastValue.set(v);
            consumer.accept(v);
        };
        var initiallyApplied = new boolean[1];
        if (applyInitially && node.getScene() == null) {
            recordingConsumer.accept(value.getValue());
            initiallyApplied[0] = true;
        }
        var listener = new WeakRefChangeListener<Consumer<T>, T>(recordingConsumer, Consumer::accept);
        var attached = new boolean[1];
        node.sceneProperty().subscribe(scene -> {
            if (scene != null && !attached[0]) {
                attached[0] = true;
                var current = value.getValue();
                var previousScene = lastScene.get() != null ? lastScene.get().get() : null;
                // The value applied before the first attach was not tied to any scene
                var sceneChanged = previousScene != scene && !initiallyApplied[0];
                initiallyApplied[0] = false;
                lastScene.set(new WeakReference<>(scene));
                // The listener is removed while detached, so only skip if the value did not change in the meantime
                if (sceneChanged || !Objects.equals(current, lastValue.get())) {
                    recordingConsumer.accept(current);
                }
                value.addListener(listener);
            } else if (scene == null && attached[0]) {
                attached[0] = false;
                value.removeListener(listener);
            }
        });
    }

    public static <R extends Region, T> void subscribeWithScene(R node, ObservableValue<T> value, Consumer<T> consumer) {
        attachWithScene(node, value, consumer, true);
    }

    public static void attach(ObservableValue<Boolean> enabled, ObservableValue<?> value, Runnable r) {
        attach(enabled, value, (ignored) -> r.run());
    }

    public static <T> void attach(ObservableValue<Boolean> enabled, ObservableValue<T> value, Consumer<T> consumer) {
        var listener = new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
                consumer.accept(newValue);
            }
        };
        enabled.subscribe(v -> {
            if (Boolean.TRUE.equals(v)) {
                consumer.accept(value.getValue());
                value.addListener(listener);
            } else {
                value.removeListener(listener);
            }
        });
    }

    public static <T> void attachList(ObservableValue<Boolean> enabled, ObservableList<T> value, Runnable consumer) {
        var listener = new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> c) {
                consumer.run();
            }
        };
        enabled.subscribe(v -> {
            if (Boolean.TRUE.equals(v)) {
                consumer.run();
                value.addListener(listener);
            } else {
                value.removeListener(listener);
            }
        });
    }

    public static <T> void attachList(
            ObservableValue<Boolean> enabled,
            ObservableList<T> value,
            Consumer<ListChangeListener.Change<? extends T>> consumer) {
        var listener = new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> c) {
                consumer.accept(c);
            }
        };
        enabled.subscribe(v -> {
            if (Boolean.TRUE.equals(v)) {
                value.addListener(listener);
            } else {
                value.removeListener(listener);
            }
        });
        consumer.accept(null);
    }

    public static <T> void listenList(
            ObservableValue<Boolean> enabled,
            ObservableList<T> value,
            Consumer<ListChangeListener.Change<? extends T>> consumer) {
        var listener = new ListChangeListener<T>() {
            @Override
            public void onChanged(Change<? extends T> c) {
                consumer.accept(c);
            }
        };
        enabled.subscribe(v -> {
            if (Boolean.TRUE.equals(v)) {
                value.addListener(listener);
            } else {
                value.removeListener(listener);
            }
        });
    }
}
