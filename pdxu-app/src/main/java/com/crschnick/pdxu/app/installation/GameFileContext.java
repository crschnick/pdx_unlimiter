package com.crschnick.pdxu.app.installation;

import com.crschnick.pdxu.app.info.SavegameInfo;
import com.crschnick.pdxu.app.info.ck2.Ck2SavegameInfo;
import com.crschnick.pdxu.app.info.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.app.info.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.app.info.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.app.info.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.app.info.vic2.Vic2SavegameInfo;
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
            SavegameType.VIC2, Game.VIC2);

    private static final Map<Class<? extends SavegameInfo<?>>, Game> INFO_MAP = Map.of(
            Eu4SavegameInfo.class, Game.EU4,
            Ck3SavegameInfo.class, Game.CK3,
            Hoi4SavegameInfo.class, Game.HOI4,
            StellarisSavegameInfo.class, Game.STELLARIS,
            Ck2SavegameInfo.class, Game.CK2,
            Vic2SavegameInfo.class, Game.VIC2);

    private final Game game;
    private final List<GameMod> mods;

    public static GameFileContext empty() {
        return new GameFileContext(null, List.of());
    }

    public static GameFileContext forGame(Game g) {
        return new GameFileContext(g, List.of());
    }

    public static GameFileContext forType(SavegameType t) {
        return new GameFileContext(TYPE_MAP.get(t), List.of());
    }

    public GameFileContext(Game game, List<GameMod> mods) {
        this.game = game;
        this.mods = mods;
    }

    public static GameFileContext fromInfo(SavegameInfo<?> info) {
        var g = INFO_MAP.get(info.getClass());
        List<GameMod> mods = info.getData().getMods() != null ? info.getData().getMods().stream()
                .map(GameInstallation.ALL.get(g)::getModForFileName)
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
