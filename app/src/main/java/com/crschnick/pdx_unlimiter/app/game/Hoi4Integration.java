package com.crschnick.pdx_unlimiter.app.game;

import com.crschnick.pdx_unlimiter.app.gui.GameGuiFactory;
import com.crschnick.pdx_unlimiter.app.gui.Hoi4GuiFactory;
import com.crschnick.pdx_unlimiter.app.savegame.SavegameCache;
import com.crschnick.pdx_unlimiter.eu4.savegame.SavegameInfo;

public class Hoi4Integration extends GameIntegration<Hoi4CampaignEntry, Hoi4Campaign> {
    @Override
    public String getName() {
        return "Hearts of Iron IV";
    }

    @Override
    public void launchCampaignEntry() {

    }

    @Override
    public boolean isVersionCompatibe(Hoi4CampaignEntry entry) {
        return true;
    }

    @Override
    public GameGuiFactory<Hoi4CampaignEntry, Hoi4Campaign> getGuiFactory() {
        return new Hoi4GuiFactory();
    }

    @Override
    public SavegameCache<? extends SavegameInfo, Hoi4CampaignEntry, Hoi4Campaign> getSavegameCache() {
        return SavegameCache.HOI4_CACHE;
    }
}
