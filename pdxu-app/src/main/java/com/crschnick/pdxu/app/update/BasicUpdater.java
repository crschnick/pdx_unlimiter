package com.crschnick.pdxu.app.update;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.core.AppInstallation;
import com.crschnick.pdxu.app.core.AppLogs;
import com.crschnick.pdxu.app.core.AppNames;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BasicUpdater extends UpdateHandler {

    public BasicUpdater(boolean thread) {
        super(thread);
    }

    @Override
    public List<ModalButton> createActions() {
        var canInstall = OsType.ofLocal() == OsType.WINDOWS
                && AppDistributionType.get() == AppDistributionType.NATIVE_INSTALLATION;

        var list = new ArrayList<ModalButton>();
        list.add(new ModalButton("ignore", null, true, false));
        list.add(new ModalButton(
                "checkOutUpdate",
                () -> {
                    var rel = getLastUpdateCheckResult().getValue();
                    if (rel == null) {
                        return;
                    }

                    Hyperlinks.open(rel.getReleaseUrl());
                },
                !canInstall,
                true));

        // On Windows, we can implement a simple autoupdater
        // This is however very basic
        if (canInstall) {
            list.add(new ModalButton(
                    "installUpdate",
                    () -> {
                        var rel = getLastUpdateCheckResult().getValue();
                        if (rel == null) {
                            return;
                        }

                        var url = rel.getRepository() + "/releases/download/" + rel.getVersion() + "/"
                                + AppNames.ofCurrent().getDistName() + "-installer-windows-"
                                + AppProperties.get().getArch() + ".msi";
                        // Note that the downloaded installer is not verified against a checksum
                        // Consider adding verification for production use
                        var systemWide = AppInstallation.ofWindows().isSystemWide();
                        var properties = "MSIFASTINSTALL=7 DISABLEROLLBACK=1" + (systemWide ? " ALLUSERS=1" : "");
                        var logFile = AppLogs.get()
                                .getSessionLogsDirectory()
                                .getParent()
                                .resolve("installer.log");
                        var command = "start \"\" /wait msiexec /i \"" + url + "\" /lv \"" + logFile + "\" /qb "
                                + properties + "&start \"\" \""
                                + AppInstallation.ofCurrent().getExecutablePath()
                                + "\"";
                        AppOperationMode.executeAfterShutdown(() -> {
                            if (LocalExec.executeAsync("cmd", "/c", command) == null) {
                                throw new IOException("Failed to launch update installer");
                            }
                        });
                    },
                    false,
                    true));
        }

        return list;
    }

    public synchronized AvailableRelease refreshUpdateCheckImpl() throws Exception {
        var found = AppReleases.getLatestSuitableRelease();
        if (found.isEmpty()) {
            lastUpdateCheckResult.setValue(null);
            return null;
        }

        var rel = found.get();
        event("Determined latest suitable release " + rel.getTagName());
        var isUpdate = isUpdate(rel.getTagName());
        var val = isUpdate
                ? new AvailableRelease(
                        AppProperties.get().getVersion(),
                        AppDistributionType.get().getId(),
                        rel.getTagName(),
                        rel.getHtmlUrl().toString(),
                        rel.getOwner().getHtmlUrl().toString(),
                        "## Changes in v" + rel.getTagName() + "\n\n" + rel.getBody(),
                        Instant.now())
                : null;
        lastUpdateCheckResult.setValue(val);
        return val;
    }
}
