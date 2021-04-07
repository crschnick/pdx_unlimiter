package com.crschnick.pdx_unlimiter.app.core;

import com.crschnick.pdx_unlimiter.app.savegame.SavegameEntry;
import com.crschnick.pdx_unlimiter.core.info.SavegameInfo;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface SavegameTool {

    List<SavegameTool> ALL = ServiceLoader
            .load(SavegameTool.class).stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());

    boolean shouldShow(SavegameEntry<?,?> entry, SavegameInfo<?> info);

    String getIconId();

    String getTooltip();

    void onClick(SavegameEntry<?,?> entry);
}
