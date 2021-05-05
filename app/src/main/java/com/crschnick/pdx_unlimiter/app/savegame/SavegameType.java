package com.crschnick.pdx_unlimiter.app.savegame;

import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;

import java.nio.file.Path;

public interface SavegameType<I extends SavegameInfo<?>> {

    String getDefaultCampaignName(I info);

    void importSavegame(byte[] data, Path output);

    String getFileEnding();

    boolean canBeUncompressed();

    Class<I> getInfoClass();

    String getInfoChecksum();
}
