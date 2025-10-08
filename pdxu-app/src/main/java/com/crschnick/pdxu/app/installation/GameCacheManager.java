package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;

import java.util.HashMap;
import java.util.Map;

public class GameCacheManager {

    private static GameCacheManager INSTANCE;
    private final Map<Class<? extends Cache>, Cache> caches = new HashMap<>();

    public static void init() {
        INSTANCE = new GameCacheManager();
    }

    public static void reset() {
        INSTANCE.caches.clear();
        INSTANCE = null;
    }

    public static GameCacheManager getInstance() {
        return INSTANCE;
    }

    public void onSelectedSavegameCollectionChange() {
        TrackEvent.debug("Clearing savegame collection caches");
        caches.entrySet().removeIf(e -> e.getValue().scope.equals(Scope.SAVEGAME_CAMPAIGN_SPECIFIC));
    }

    @SuppressWarnings("unchecked")
    public <T extends Cache> T get(Class<T> clazz) {
        try {
            if (caches.containsKey(clazz)) {
                return (T) caches.get(clazz);
            } else {
                var cache = (T) clazz.getConstructors()[0].newInstance();
                caches.put(clazz, cache);
                return cache;
            }
        } catch (Exception e) {
            ErrorEventFactory.fromThrowable(e).handle();
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
