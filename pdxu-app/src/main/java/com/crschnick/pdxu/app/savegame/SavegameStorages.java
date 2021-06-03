package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.lang.GameLocalisation;
import com.crschnick.pdxu.io.savegame.SavegameType;
import com.crschnick.pdxu.model.GameDateType;
import com.crschnick.pdxu.model.ck2.Ck2SavegameInfo;
import com.crschnick.pdxu.model.ck3.Ck3SavegameInfo;
import com.crschnick.pdxu.model.eu4.Eu4SavegameInfo;
import com.crschnick.pdxu.model.hoi4.Hoi4SavegameInfo;
import com.crschnick.pdxu.model.stellaris.StellarisSavegameInfo;
import com.crschnick.pdxu.model.vic2.Vic2SavegameInfo;

public class SavegameStorages {

    public static void init() throws Exception {
        SavegameStorage.ALL.put(Game.EU4, new SavegameStorage<>(
                (node, melted) -> Eu4SavegameInfo.fromSavegame(melted, node),
                "eu4",
                GameDateType.EU4,
                SavegameType.EU4,
                Eu4SavegameInfo.class) {
            @Override
            protected String getDefaultCampaignName(Eu4SavegameInfo info) {
                return GameLocalisation.getLocalisedValue(info.getTag().getTag(), info);
            }
        });
        SavegameStorage.ALL.put(Game.HOI4, new SavegameStorage<>(
                (node, melted) -> Hoi4SavegameInfo.fromSavegame(melted, node),
                "hoi4",
                GameDateType.HOI4,
                SavegameType.HOI4,
                Hoi4SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Hoi4SavegameInfo info) {
                return "Unknown";
            }
        });
        SavegameStorage.ALL.put(Game.CK3, new SavegameStorage<>(
                (node, melted) -> Ck3SavegameInfo.fromSavegame(melted, node),
                "ck3",
                GameDateType.CK3,
                SavegameType.CK3,
                Ck3SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Ck3SavegameInfo info) {
                if (info.isObserver()) {
                    return "Observer";
                }

                if (!info.hasOnePlayerTag()) {
                    return "Unknown";
                }

                return info.getTag().getName();
            }
        });
        SavegameStorage.ALL.put(Game.STELLARIS, new SavegameStorage<>(
                (node, melted) -> StellarisSavegameInfo.fromSavegame(node),
                "stellaris",
                GameDateType.STELLARIS,
                SavegameType.STELLARIS,
                StellarisSavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(StellarisSavegameInfo info) {
                return info.getTag().getName();
            }
        });
        SavegameStorage.ALL.put(Game.CK2, new SavegameStorage<>(
                (node, melted) -> new Ck2SavegameInfo(node),
                "ck2",
                GameDateType.CK2,
                SavegameType.CK2,
                Ck2SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Ck2SavegameInfo info) {
                return info.getTag().getRulerName();
            }
        });
        SavegameStorage.ALL.put(Game.VIC2, new SavegameStorage<>(
                (node, melted) -> new Vic2SavegameInfo(node),
                "vic2",
                GameDateType.VIC2,
                SavegameType.VIC2,
                Vic2SavegameInfo.class
        ) {
            @Override
            protected String getDefaultCampaignName(Vic2SavegameInfo info) {
                return "Unknown";
            }
        });
    }
}
