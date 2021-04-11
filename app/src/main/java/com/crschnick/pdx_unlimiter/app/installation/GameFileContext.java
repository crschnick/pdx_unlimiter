package com.crschnick.pdx_unlimiter.app.installation;

import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdx_unlimiter.core.info.stellaris.StellarisSavegameInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameFileContext {

    private static final Map<Class<? extends SavegameInfo<?>>, Game> INFO_MAP = Map.of(
            Eu4SavegameInfo.class, Game.EU4,
            Ck3SavegameInfo.class, Game.CK3,
            Hoi4SavegameInfo.class, Game.HOI4,
            StellarisSavegameInfo.class, Game.STELLARIS);

    public static GameFileContext empty() {
        return new GameFileContext(null, List.of());
    }

    public static GameFileContext forGame(Game g) {
        return new GameFileContext(g, List.of());
    }

    public static GameFileContext fromInfo(SavegameInfo<?> info) {
        var g = INFO_MAP.get(info.getClass());
        return new GameFileContext(g, info.getMods().stream()
                .map(GameInstallation.ALL.get(g)::getModForName)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    private final Game game;
    private final List<GameMod> mods;

    public GameFileContext(Game game, List<GameMod> mods) {
        this.game = game;
        this.mods = mods;
    }

    public Game getGame() {
        return game;
    }

    public GameInstallation getInstall() {
        return game != null ? GameInstallation.ALL.get(game) : null;
    }

    public List<GameMod> getMods() {
        return mods;
    }
}
