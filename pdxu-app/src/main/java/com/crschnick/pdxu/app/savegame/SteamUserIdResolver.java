package com.crschnick.pdxu.app.savegame;

import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.core.AppLayoutModel;
import com.crschnick.pdxu.app.installation.dist.SteamDist;
import com.crschnick.pdxu.app.platform.LabelGraphic;
import com.crschnick.pdxu.app.platform.PlatformThread;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class SteamUserIdResolver {

    private SteamUserIdResolver() {}

    public static long resolveOrRandom() {
        var userId = SteamDist.determineSteamUserId(id -> showWarning("steamMostRecentUserUsed", id));
        if (userId.isPresent()) {
            return userId.getAsLong();
        }

        showWarning("steamUserIdNotDetected");
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    private static void showWarning(String key, Object... arguments) {
        PlatformThread.runLaterIfNeeded(() -> {
            var layout = AppLayoutModel.get();
            if (layout != null) {
                layout.showQueueEntry(
                        new AppLayoutModel.QueueEntry(
                                AppI18n.observable(key, arguments),
                                new LabelGraphic.IconGraphic("mdomz-warning"),
                                () -> {}),
                        Duration.ofSeconds(15),
                        true);
            }
        });
    }
}
