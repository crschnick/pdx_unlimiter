package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameWatcher;
import com.crschnick.pdx_unlimiter.core.savegame.SavegameInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class GameIntegration<T, I extends SavegameInfo<T>> {

    public static Eu4Integration EU4;
    public static Hoi4Integration HOI4;
    public static StellarisIntegration STELLARIS;
    public static Ck3Integration CK3;

    public static List<GameIntegration<?, ? extends SavegameInfo<?>>> ALL;

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> getForInstallation(GameInstallation i) {
        for (var g : ALL) {
            if (g.getInstallation().equals(i)) {
                return (GameIntegration<T, I>) g;
            }
        }
        throw new IllegalArgumentException();
    }

    public static <T, I extends SavegameInfo<T>> GameIntegration<T, I> getForSavegameCache(SavegameCache<T, I> c) {
        for (var g : ALL) {
            if (g.getSavegameCache().equals(c)) {
                return (GameIntegration<T, I>) g;
            }
        }
        throw new IllegalArgumentException();
    }


    public static void init() {
        ALL = new ArrayList<>();
        if (GameInstallation.EU4 != null) {
            EU4 = new Eu4Integration();
            ALL.add(EU4);
        }
//        if (GameInstallation.HOI4 != null) {
//            HOI4 = new Hoi4Integration();
//            ALL.add(HOI4);
//        }

        if (GameInstallation.STELLARIS != null) {
            STELLARIS = new StellarisIntegration();
            ALL.add(STELLARIS);
        }

        if (GameInstallation.CK3 != null) {
            CK3 = new Ck3Integration();
            ALL.add(CK3);
        }
    }

    public static void reset() {
        ALL.clear();
        EU4 = null;
        CK3 = null;
        STELLARIS = null;
        HOI4 = null;
    }

    public abstract String getName();

    public abstract GameInstallation getInstallation();

    public abstract GameGuiFactory<T, I> getGuiFactory();

    public abstract SavegameCache<T, I> getSavegameCache();

    public abstract SavegameWatcher getSavegameWatcher();
}
