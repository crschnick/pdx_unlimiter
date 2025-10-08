package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.model.GameVersion;

import java.util.Optional;

public class SavegameCompatibility {

    public static Compatbility determineForModsAndDLCs(SavegameEntry<?, ?> entry) {
        return SavegameContext.mapSavegame(entry, ctx -> {
            var info = ctx.getInfo();
            if (info == null) {
                return Compatbility.INCOMPATIBLE;
            }

            var ins = ctx.getInstallation();
            boolean missingMods = info.getData().getMods() != null && info.getData().getMods().stream()
                    .map(ins::getModForSavegameId)
                    .anyMatch(Optional::isEmpty);

            boolean missingDlc = info.getData().getDlcs() != null && info.getData().getDlcs().stream()
                    .map(ins::getDlcForSavegameId)
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

    public static Compatbility determineForVersion(Game game, GameVersion version) {
        var i = GameInstallation.ALL.get(game);
        if (i.getVersion() == null) {
            return Compatbility.UNKNOWN;
        }

        return areCompatible(
                GameInstallation.ALL.get(game).getVersion(),
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
