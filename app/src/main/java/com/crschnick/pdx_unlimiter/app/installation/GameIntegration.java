package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.app.gui.game.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.installation.game.Ck3Integration;
import com.crschnick.pdx_unlimiter.app.installation.game.Eu4Integration;
import com.crschnick.pdx_unlimiter.app.installation.game.Hoi4Integration;
import com.crschnick.pdx_unlimiter.app.installation.game.StellarisIntegration;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameStorage;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class GameIntegration<T, I extends SavegameInfo<T>> {

    public static Map<Game,GameIntegration<?, ? extends SavegameInfo<?>>> ALL;

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> getForInstallation(GameInstallation i) {
        for (var g : ALL.values()) {
            if (g.getInstallation().equals(i)) {
                return (GameIntegration<T, I>) g;
            }
        }
        throw new IllegalArgumentException();
    }

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> getForSavegameStorage(SavegameStorage<T, I> c) {
        for (var g : ALL.values()) {
            if (g.getSavegameStorage().equals(c)) {
                return (GameIntegration<T, I>) g;
            }
        }
        return null;
    }


    public static void init() {
        ALL = new HashMap<>();
        if (GameInstallation.ALL.get(Game.EU4) != null) {
            ALL.put(Game.EU4, new Eu4Integration());
        }
        if (GameInstallation.ALL.get(Game.CK3) != null) {
            ALL.put(Game.CK3, new Ck3Integration());
        }
        if (GameInstallation.ALL.get(Game.HOI4) != null) {
            ALL.put(Game.HOI4, new Hoi4Integration());
        }
        if (GameInstallation.ALL.get(Game.STELLARIS) != null) {
            ALL.put(Game.STELLARIS, new StellarisIntegration());
        }
    }

    public static void reset() {
        ALL.clear();
    }

    public abstract String getName();

    public abstract GameInstallation getInstallation();

    public abstract GameGuiFactory<T, I> getGuiFactory();

    public abstract SavegameStorage<T, I> getSavegameStorage();

    public abstract SavegameWatcher getSavegameWatcher();
}
