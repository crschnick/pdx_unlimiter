package com.crschnick.pdxu.app.update;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.AppVersion;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.prefs.AppPrefs;
import com.crschnick.pdxu.app.util.BooleanScope;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@SuppressWarnings("InfiniteLoopStatement")
@Getter
public abstract class UpdateHandler {

    protected final Property<AvailableRelease> lastUpdateCheckResult = new SimpleObjectProperty<>();
    protected final BooleanProperty busy = new SimpleBooleanProperty();

    protected UpdateHandler(boolean startBackgroundThread) {
        if (startBackgroundThread) {
            startBackgroundUpdater();
        }

        lastUpdateCheckResult.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Show available update in PTB more aggressively
                if (!AppOperationMode.isInStartup()) {
                    UpdateAvailableDialog.showIfNeeded(false);
                }
            }
        });
    }

    private void startBackgroundUpdater() {
        ThreadHelper.createPlatformThread("updater", true, () -> {
                    ThreadHelper.sleep(Duration.ofMinutes(1).toMillis());
                    event("Starting background updater thread");
                    while (true) {
                        if (AppPrefs.get() != null
                                && AppPrefs.get().automaticallyUpdate().get()) {
                            event("Performing background update");
                            refreshUpdateCheckSilent();
                        }

                        ThreadHelper.sleep(Duration.ofHours(1).toMillis());
                    }
                })
                .start();
    }

    protected void event(String msg) {
        TrackEvent.builder().type("info").message(msg).handle();
    }

    protected final boolean isUpdate(String releaseVersion) {
        var canonical = AppVersion.parse(releaseVersion);
        // Don't show v2 updates
        if (canonical.isPresent() && canonical.get().getMajor() < 3) {
            return false;
        }

        if (!AppProperties.get().getVersion().equals(releaseVersion)) {
            event("Release has a different version");
            return true;
        }

        return false;
    }

    public final AvailableRelease refreshUpdateCheckSilent() {
        try {
            return refreshUpdateCheck();
        } catch (Exception ex) {
            ErrorEventFactory.fromThrowable(ex).discard().handle();
            return null;
        }
    }

    public abstract List<ModalButton> createActions();

    public final AvailableRelease refreshUpdateCheck() throws Exception {
        if (busy.getValue()) {
            return lastUpdateCheckResult.getValue();
        }

        try (var ignored = new BooleanScope(busy).start()) {
            return refreshUpdateCheckImpl();
        }
    }

    public abstract AvailableRelease refreshUpdateCheckImpl() throws Exception;

    @Value
    @Builder
    @Jacksonized
    @With
    public static class AvailableRelease {
        String sourceVersion;
        String sourceDist;
        String version;
        String releaseUrl;
        String repository;
        String body;
        Instant checkTime;
    }
}
