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

    public static GameFileContext fromInfo(SavegameInfo<?> info) {
        var install = GameInstallation.ALL.get(INFO_MAP.get(info.getClass()));
        return new GameFileContext(
                install,
                info.getMods().stream()
                .map(install::getModForName)
                .flatMap(Optional::stream)
                .collect(Collectors.toList()));
    }

    private final GameInstallation install;
    private final List<GameMod> mods;

    public GameFileContext(GameInstallation install, List<GameMod> mods) {
        this.install = install;
        this.mods = mods;
    }

    public GameInstallation getInstall() {
        return install;
    }

    public List<GameMod> getMods() {
        return mods;
    }
}
