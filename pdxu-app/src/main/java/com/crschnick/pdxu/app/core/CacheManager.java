package com.crschnick.pdxu.app.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    private static final Logger logger = LoggerFactory.getLogger(CacheManager.class);
    private static CacheManager INSTANCE;
    private final Map<Class<? extends Cache>, Cache> caches = new HashMap<>();

    public static void init() {
        INSTANCE = new CacheManager();
    }

    public static void reset() {
        INSTANCE.caches.clear();
        INSTANCE = null;
    }

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    public void onSelectedGameChange() {
        logger.debug("Clearing game caches");
        caches.clear();
    }

    public void onSelectedSavegameCollectionChange() {
        logger.debug("Clearing savegame collection caches");
        caches.entrySet().removeIf(e -> e.getValue().scope.equals(Scope.SAVEGAME_CAMPAIGN_SPECIFIC));
    }

    @SuppressWarnings("unchecked")
    public <T extends Cache> T get(Class<T> clazz) {
        var sc = SavegameManagerState.get().globalSelectedCollectionProperty().get();

        try {
            if (caches.containsKey(clazz)) {
                return (T) caches.get(clazz);
            } else {
                var cache = (T) clazz.getConstructors()[0].newInstance();
                caches.put(clazz, cache);
                return cache;
            }
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            return null;
        }
    }

    public enum Scope {
        SAVEGAME_CAMPAIGN_SPECIFIC,
        GAME_SPECIFIC
    }

    public static class Cache {

        private final Scope scope;

        public Cache(Scope scope) {
            this.scope = scope;
        }
    }
}
