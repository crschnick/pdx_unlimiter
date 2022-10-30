package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.info.SavegameData;
import com.crschnick.pdxu.app.info.ck2.Ck2SavegameData;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameData;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameData;
import com.crschnick.pdxu.app.info.hoi4.Hoi4SavegameData;
import com.crschnick.pdxu.app.info.stellaris.StellarisSavegameData;
import com.crschnick.pdxu.app.info.vic2.Vic2SavegameData;
import com.crschnick.pdxu.app.info.vic3.Vic3SavegameData;
import com.crschnick.pdxu.app.savegame.SavegameStorage;
import com.crschnick.pdxu.io.parser.TextFormatParser;
import com.crschnick.pdxu.io.savegame.SavegameType;

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
            SavegameType.VIC2, Game.VIC2,
            SavegameType.VIC3, Game.VIC3
    );

    public  static final Map<Class<? extends SavegameData<?>>, Game> INFO_MAP = Map.of(
            Eu4SavegameData.class, Game.EU4,
            Hoi4SavegameData.class, Game.HOI4,
            Ck2SavegameData.class, Game.CK2,
            Vic2SavegameData.class, Game.VIC2,
            Vic3SavegameData.class, Game.VIC3,
            StellarisSavegameData.class, Game.STELLARIS,
            Ck3SavegameData .class, Game.CK3);

    private final Game game;
    private final List<GameMod> mods;

    public static GameFileContext empty() {
        return new GameFileContext(null, List.of());
    }

    public static GameFileContext forGame(Game g) {
        return new GameFileContext(g, List.of());
    }

    public static GameFileContext forGameAndMods(Game g, List<GameMod> mods) {
        return new GameFileContext(g, mods);
    }

    public static GameFileContext forType(SavegameType t) {
        return new GameFileContext(TYPE_MAP.get(t), List.of());
    }

    public GameFileContext(Game game, List<GameMod> mods) {
        this.game = game;
        this.mods = mods;
    }

    public static GameFileContext fromData(SavegameData<?> sgData) {
        var g = INFO_MAP.get(sgData.getClass());
        List<GameMod> mods = sgData.getMods() != null ? sgData.getMods().stream()
                .map(GameInstallation.ALL.get(g)::getModForSavegameId)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()) : List.of();
        return new GameFileContext(g, mods);
    }

    public TextFormatParser getParser() {
        if (getGame() == null) {
            return TextFormatParser.text();
        }

        return SavegameStorage.get(getGame()).getType().getParser();
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
