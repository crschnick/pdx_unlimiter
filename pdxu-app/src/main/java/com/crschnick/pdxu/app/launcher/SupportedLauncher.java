package com.crschnick.pdxu.app.launcher;

import com.crschnick.pdxu.app.core.ErrorHandler;
import com.crschnick.pdxu.app.core.SavegameManagerState;
import com.crschnick.pdxu.app.core.settings.Settings;
import com.crschnick.pdxu.app.installation.Game;
import com.crschnick.pdxu.app.installation.GameInstallation;
import com.crschnick.pdxu.app.lang.Language;
import com.crschnick.pdxu.app.util.integration.IronyHelper;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualLinkedHashBidiMap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class SupportedLauncher {

    public static final SupportedLauncher IRONY = new IronyLauncher();
    public static final SupportedLauncher PDXU = new PdxuLauncher();
    public static final SupportedLauncher PARADOX = new ParadoxLauncher();
    public static final SupportedLauncher DEFAULT = PDXU;

    public static BidiMap<String, SupportedLauncher> getAllLaunchers() {
        var map = new LinkedHashMap<String, SupportedLauncher>();
        map.put("pdxu", PDXU);
        map.put("paradox", PARADOX);
        map.put("irony", IRONY);
        return new DualLinkedHashBidiMap<>(map);
    }

    public static void startLauncher(Game game, boolean continueGame) {
        var used = getUsedLauncher(game);
        try {
            used.start(game);
        } catch (IOException e) {
            ErrorHandler.handleException(e);
        }
    }

    public static SupportedLauncher getUsedLauncher(Game game) {
        if (Settings.getInstance().launcher.getValue() == IRONY && IRONY.isSupported(game)) {
            return IRONY;
        }

        if (Settings.getInstance().launcher.getValue() == PARADOX && PARADOX.isSupported(game)) {
            return IRONY;
        }

        return PDXU;
    }

    public abstract boolean isSupported(Game game);

    public abstract void start(Game game) throws IOException;

    public abstract String getDisplayName();
}
