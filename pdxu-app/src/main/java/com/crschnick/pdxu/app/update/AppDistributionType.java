package com.crschnick.pdxu.app.update;

import com.crschnick.pdxu.app.core.*;
import com.crschnick.pdxu.app.issue.ErrorEventFactory;
import com.crschnick.pdxu.app.issue.TrackEvent;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;
import com.crschnick.pdxu.app.util.Translatable;

import javafx.beans.value.ObservableValue;

import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Supplier;

public enum AppDistributionType implements Translatable {
    UNKNOWN("unknown", false, () -> new BasicUpdater(false)),
    DEVELOPMENT("development", true, () -> new BasicUpdater(false)),
    PORTABLE("portable", false, () -> new BasicUpdater(true)),
    NATIVE_INSTALLATION("install", true, () -> new BasicUpdater(true));

    private static AppDistributionType type;

    @Getter
    private final String id;

    @Getter
    private final boolean supportsUrls;

    private final Supplier<UpdateHandler> updateHandlerSupplier;
    private UpdateHandler updateHandler;

    AppDistributionType(String id, boolean supportsUrls, Supplier<UpdateHandler> updateHandlerSupplier) {
        this.id = id;
        this.supportsUrls = supportsUrls;
        this.updateHandlerSupplier = updateHandlerSupplier;
    }

    public static void init() {
        if (type != null) {
            return;
        }

        if (!AppProperties.get().isImage()) {
            type = DEVELOPMENT;
            return;
        }

        if (!AppProperties.get().isNewBuildSession() && !isDifferentDaemonExecutable()) {
            var cached = AppCache.getNonNull("dist", String.class, () -> null);
            var cachedType = Arrays.stream(values())
                    .filter(d -> d.getId().equals(cached))
                    .findAny()
                    .orElse(null);
            if (cachedType != null) {
                type = cachedType;
                return;
            }
        }

        var det = determine();

        // Don't cache unknown type
        if (det == UNKNOWN) {
            return;
        }

        type = det;
        AppCache.update("dist", type.getId());
        TrackEvent.withInfo("Determined distribution type")
                .tag("type", type.getId())
                .handle();
    }

    private static boolean isDifferentDaemonExecutable() {
        var cached = AppCache.getNonNull("daemonExecutable", String.class, () -> null);
        var current = AppInstallation.ofCurrent().getExecutablePath().toString();
        if (current.equals(cached)) {
            return false;
        }

        AppCache.update("daemonExecutable", current);
        return true;
    }

    public static AppDistributionType get() {
        if (type == null) {
            return UNKNOWN;
        }

        return type;
    }

    public static AppDistributionType determine() {
        var base = AppInstallation.ofCurrent().getBaseInstallationPath();
        if (OsType.ofLocal() == OsType.MACOS) {
            if (!base.equals(AppInstallation.ofDefault().getBaseInstallationPath())) {
                return PORTABLE;
            }

            try {
                var r = LocalExec.readStdoutIfPossible(
                        "pkgutil",
                        "--pkg-info",
                        AppNames.ofCurrent().getGroupName() + "."
                                + AppNames.ofCurrent().getKebapName());
                if (r.isEmpty()) {
                    return PORTABLE;
                }
            } catch (Exception ex) {
                ErrorEventFactory.fromThrowable(ex).omit().handle();
                return PORTABLE;
            }
        } else {
            var file = base.resolve("installation");
            if (!Files.exists(file)) {
                return PORTABLE;
            }
        }

        // Fix for community AUR builds that use the RPM dist
        if (OsType.ofLocal() == OsType.LINUX && Files.exists(Path.of("/etc/arch-release"))) {
            return PORTABLE;
        }

        return AppDistributionType.NATIVE_INSTALLATION;
    }

    public UpdateHandler getUpdateHandler() {
        if (updateHandler == null) {
            updateHandler = updateHandlerSupplier.get();
        }
        return updateHandler;
    }

    @Override
    public ObservableValue<String> toTranslatedString() {
        return AppI18n.observable(getId() + "Dist");
    }
}
