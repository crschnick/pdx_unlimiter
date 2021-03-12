package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameFolder;
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

    public void onGameChange() {
        logger.debug("Clearing game caches");
        caches.clear();
    }

    public void onSavegameCollectionChange() {
        logger.debug("Clearing savegame collection caches");
        caches.entrySet().removeIf(e -> !e.getValue().scope.equals(Scope.GAME));
    }

    public void onSavegameLoad() {
        if (SavegameManagerState.get().globalSelectedCollectionProperty().get() instanceof SavegameFolder) {
            logger.debug("Clearing savegame collection caches");
            caches.entrySet().removeIf(e -> e.getValue().scope.equals(Scope.SAVEGAME_CAMPAIGN));
        }
        logger.debug("Clearing savegame caches");
        caches.entrySet().removeIf(e -> e.getValue().scope.equals(Scope.SAVEGAME));
    }

    @SuppressWarnings("unchecked")
    public <T extends Cache> T get(Class<T> clazz) {
        try {
            return (T) caches.getOrDefault(clazz, (Cache) clazz.getConstructors()[0].newInstance());
        } catch (Exception e) {
            ErrorHandler.handleException(e);
            return null;
        }
    }

    public enum Scope {
        SAVEGAME,
        SAVEGAME_CAMPAIGN,
        GAME
    }

    public static class Cache {

        private final Scope scope;

        public Cache(Scope scope) {
            this.scope = scope;
        }
    }
}
