package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.model.GameVersion;
import com.crschnick.pdxu.model.SavegameInfo;

import java.util.Optional;

public class SavegameCompatibility {

    public static Compatbility determineForEntry(SavegameEntry<?, ?> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            var info = ctx.getInfo();
            if (info == null) {
                return Compatbility.INCOMPATIBLE;
            }

            var ins = ctx.getInstallation();
            boolean missingMods = info.getMods() != null && info.getMods().stream()
                    .map(ins::getModForSavegameId)
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

    public static Compatbility determineForInfo(GameVersion version) {
        var i = GameInstallation.ALL.get(SavegameManagerState.get().current());
        if (i.getVersion() == null) {
            return Compatbility.UNKNOWN;
        }

        return areCompatible(
                GameInstallation.ALL.get(SavegameManagerState.get().current()).getVersion(),
                version) ? Compatbility.COMPATIBLE : Compatbility.INCOMPATIBLE;
    }

    private static boolean areCompatible(GameVersion gameVersion, GameVersion saveVersion) {
        return gameVersion.getFirst() == saveVersion.getFirst() && gameVersion.getSecond() == saveVersion.getSecond();
    }

    public enum Compatbility {
        COMPATIBLE,
        INCOMPATIBLE,
        UNKNOWN
    }
}
