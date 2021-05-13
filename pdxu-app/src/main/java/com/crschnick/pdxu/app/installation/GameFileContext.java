package com.crschnick.pdxu.app.installation;

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

    private static final Map<Class<? extends SavegameInfo<?>>, Game> INFO_MAP = Map.of(
            Eu4SavegameInfo.class, Game.EU4,
            Ck3SavegameInfo.class, Game.CK3,
            Hoi4SavegameInfo.class, Game.HOI4,
            StellarisSavegameInfo.class, Game.STELLARIS);
    private final GameInstallation install;
    private final List<GameMod> mods;

    public GameFileContext(GameInstallation install, List<GameMod> mods) {
        this.install = install;
        this.mods = mods;
    }

    public static GameFileContext fromInfo(SavegameInfo<?> info) {
        var install = GameInstallation.ALL.get(INFO_MAP.get(info.getClass()));
        return new GameFileContext(
                install,
                info.getMods().stream()
                        .map(install::getModForId)
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()));
    }

    public GameInstallation getInstall() {
        return install;
    }

    public List<GameMod> getMods() {
        return mods;
    }
}
