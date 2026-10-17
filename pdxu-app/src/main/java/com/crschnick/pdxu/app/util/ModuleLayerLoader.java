package com.crschnick.pdxu.app.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Consumer;

public interface ModuleLayerLoader {

    Set<ModuleLayer> LOADED_LAYERS = Collections.synchronizedSet(new HashSet<>());

    static void loadAll(ModuleLayer layer, Consumer<Throwable> errorHandler) {
        // Error handlers also call this to make sure that everything is initialized
        if (!LOADED_LAYERS.add(layer)) {
            return;
        }

        var loaded = layer != null
                ? ServiceLoader.load(layer, ModuleLayerLoader.class)
                : ServiceLoader.load(ModuleLayerLoader.class);
        loaded.stream().forEach(moduleLayerLoaderProvider -> {
            try {
                var instance = moduleLayerLoaderProvider.get();
                instance.init(layer);
            } catch (Throwable t) {
                errorHandler.accept(t);
            }
        });
    }

    default void init(ModuleLayer layer) {}
}
