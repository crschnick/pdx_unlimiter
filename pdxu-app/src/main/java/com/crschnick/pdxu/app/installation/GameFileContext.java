package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.SavegameInfo;
import com.crschnick.pdxu.model.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.model.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.model.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisSavegameInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameFileContext {

    private static final Map<SavegameType, Game> TYPE_MAP = Map.of(
            SavegameType.EU4, Game.EU4,
            SavegameType.CK3, Game.CK3,
            SavegameType.HOI4, Game.HOI4,
            SavegameType.STELLARIS, Game.STELLARIS,
            SavegameType.CK2, Game.CK2,
            SavegameType.VIC2, Game.VIC2);

    private static final Map<Class<? extends SavegameInfo<?>>, Game> INFO_MAP = Map.of(
            Eu4SavegameInfo.class, Game.EU4,
            Ck3SavegameInfo.class, Game.CK3,
            Hoi4SavegameInfo.class, Game.HOI4,
            StellarisSavegameInfo.class, Game.STELLARIS);
    private final Game game;
    private final List<GameMod> mods;

    public static GameFileContext empty() {
        return new GameFileContext(null, List.of());
    }

    public static GameFileContext forGame(Game g) {
        return new GameFileContext(g, List.of());
    }

    public GameFileContext(Game game, List<GameMod> mods) {
        this.game = game;
        this.mods = mods;
    }

    public static GameFileContext fromInfo(SavegameInfo<?> info) {
        var g = INFO_MAP.get(info.getClass());
        return new GameFileContext(g, info.getMods().stream()
                .map(GameInstallation.ALL.get(g)::getModForFileName)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
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
