package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.app.core.SavegameManagerState;
import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.crschnick.pdx_unlimiter.core.info.GameVersion;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;

import java.util.Optional;

public class SavegameCompatibility {

    public static enum Compatbility {
        COMPATIBLE,
        INCOMPATIBLE,
        UNKNOWN
    }

    public static Compatbility determineForEntry(SavegameEntry<?, ?> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            var info = ctx.getInfo();
            if (info == null) {
                return Compatbility.INCOMPATIBLE;
            }

            var ins = ctx.getInstallation();
            boolean missingMods = info.getMods().stream()
                    .map(ins::getModForId)
                    .anyMatch(Optional::isEmpty);

            boolean missingDlc = info.getDlcs().stream()
                    .map(ins::getDlcForName)
                    .anyMatch(Optional::isEmpty);

            if (missingMods || missingDlc) {
                return Compatbility.INCOMPATIBLE;
            }

            if (ins.getVersion() == null) {
                return Compatbility.UNKNOWN;
            }

            return Compatbility.COMPATIBLE;
        });
    }

    public static Compatbility determineForInfo(SavegameInfo<?> info) {
        var i = GameInstallation.ALL.get(SavegameManagerState.get().current());
        if (i.getVersion() == null) {
            return Compatbility.UNKNOWN;
        }

        return areCompatible(
                GameInstallation.ALL.get(SavegameManagerState.get().current()).getVersion(),
                info.getVersion()) ? Compatbility.COMPATIBLE : Compatbility.INCOMPATIBLE;
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }
}
