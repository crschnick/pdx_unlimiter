package com.crschnick.pdxu.app.update;

import com.crschnick.pdxu.app.comp.base.ModalButton;
import com.crschnick.pdxu.app.core.AppInstallation;
import com.crschnick.pdxu.app.core.AppNames;
import com.crschnick.pdxu.app.core.AppProperties;
import com.crschnick.pdxu.app.core.mode.AppOperationMode;
import com.crschnick.pdxu.app.util.Hyperlinks;
import com.crschnick.pdxu.app.util.LocalExec;
import com.crschnick.pdxu.app.util.OsType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BasicUpdater extends UpdateHandler {

    public BasicUpdater(boolean thread) {
        super(thread);
    }

    @Override
    public List<ModalButton> createActions() {
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
                false,
                true));

        // On Windows, we can implement a simple autoupdater
        // This is however very basic
        if (OsType.ofLocal() == OsType.WINDOWS && AppDistributionType.get() == AppDistributionType.NATIVE_INSTALLATION) {
            list.add(new ModalButton(
                    "installUpdate",
                    () -> {
                        var rel = getLastUpdateCheckResult().getValue();
                        if (rel == null) {
                            return;
                        }

                        var url = rel.getRepository() + "/releases/download/" + rel.getVersion() + "/" +
                                AppNames.ofCurrent().getDistName() + "-installer-windows-" + AppProperties.get().getArch() + ".msi";
                        AppOperationMode.executeAfterShutdown(() -> {
                            var command = "set MSIFASTINSTALL=7&set DISABLEROLLBACK=1&start \"\" /wait msiexec /i \"" + url + "\" /qb&start \"\" \"" + AppInstallation.ofCurrent().getExecutablePath() + "\"";
                            LocalExec.executeAsync("cmd", "/c", command);
                        });
                    },
                    false,
                    true));
        }

        return list;
    }

    public synchronized AvailableRelease refreshUpdateCheckImpl() throws Exception {
        var found = AppReleases.getMarkedLatestRelease();
        if (found.isEmpty()) {
            return null;
        }

        var rel = found.get();
        event("Determined latest suitable release " + rel.getTagName());
        var isUpdate = isUpdate(rel.getTagName());
        var val = isUpdate ? new AvailableRelease(
                AppProperties.get().getVersion(),
                AppDistributionType.get().getId(),
                rel.getTagName(),
                rel.getHtmlUrl().toString(),
                rel.getOwner().getHtmlUrl().toString(),
                "## Changes in v" + rel.getTagName() + "\n\n" + rel.getBody(),
                Instant.now()) : null;
        lastUpdateCheckResult.setValue(val);
        return lastUpdateCheckResult.getValue();
    }
}
