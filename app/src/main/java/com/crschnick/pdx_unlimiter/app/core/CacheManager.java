package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameManagerState;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CacheManager {

    public static enum Scope {
        SAVEGAME,
        SAVEGAME_COLLECTION,
        GAME
    }

    public static class Cache {

        private Scope scope;

        public Cache(Scope scope) {
            this.scope = scope;
        }
    }

    private static CacheManager INSTANCE;

    public static void init() {
        INSTANCE = new CacheManager();
        SavegameManagerState.get().currentGameProperty().addListener((c,o,n) -> {
            INSTANCE.caches.clear();
        });

        SavegameManagerState.get().globalSelectedCampaignProperty().addListener((c,o,n) -> {
            INSTANCE.caches.entrySet().removeIf(e -> !e.getValue().scope.equals(Scope.GAME));
        });

        SavegameManagerState.get().globalSelectedEntryProperty().addListener((c,o,n) -> {
            INSTANCE.caches.entrySet().removeIf(e -> e.getValue().scope.equals(Scope.SAVEGAME));
        });
    }

    public static void reset() {
        INSTANCE.caches.clear();
        INSTANCE = null;
    }

    public static CacheManager getInstance() {
        return INSTANCE;
    }

    private Map<Class<? extends Cache>,Cache> caches = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends Cache> T get(Class<T> clazz) {
        try {
            return (T) caches.getOrDefault(clazz, (Cache) clazz.getConstructors()[0].newInstance());
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            return null;
        }
    }
}
