package com.crschnick.pdxu.app.launcher;

import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.installation.dist.GameDistLauncher;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ParadoxLauncher extends SupportedLauncher {

    @Override
    public boolean isSupported(Game game) {
        return false;
    }

    @Override
    public void start(Game game) throws IOException {
        var dist = GameInstallation.ALL.get(game).getDist();
        if (dist.supportsLauncher()) {
            GameInstallation.ALL.get(game).getDist().startLauncher(Map.of());
        }
    }

    @Override
    public String getDisplayName() {
        return "Paradox Launcher";
    }
}
