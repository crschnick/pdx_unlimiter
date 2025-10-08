package com.crschnick.pdxu.app.prefs;

import com.crschnick.pdxu.app.comp.SimpleComp;
import com.crschnick.pdxu.app.comp.base.TileButtonComp;
import com.crschnick.pdxu.app.core.AppI18n;
import com.crschnick.pdxu.app.update.AppDistributionType;
import com.crschnick.pdxu.app.update.UpdateAvailableDialog;
import com.crschnick.pdxu.app.util.ThreadHelper;

import javafx.beans.binding.Bindings;
import javafx.scene.layout.Region;

public class UpdateCheckComp extends SimpleComp {

    @Override
    protected Region createSimple() {
        var uh = AppDistributionType.get().getUpdateHandler();
        var name = Bindings.createStringBinding(
                () -> {
                    var available = uh.getLastUpdateCheckResult().getValue();
                    if (available != null) {
                        return AppI18n.get("updateReady", available.getVersion());
                    }

                    if (uh.getBusy().getValue()) {
                        return AppI18n.get("checkingForUpdates");
                    }

                    return AppI18n.get("checkForUpdates");
                },
                AppI18n.activeLanguage(),
                uh.getLastUpdateCheckResult(),
                uh.getBusy());
        var description = Bindings.createStringBinding(
                () -> {
                    var available = uh.getLastUpdateCheckResult().getValue();
                    if (available != null) {
                        return AppI18n.get("updateReadyDescription");
                    }

                    if (uh.getBusy().getValue()) {
                        return AppI18n.get("checkingForUpdatesDescription");
                    }

                    return AppI18n.get("checkForUpdatesDescription");
                },
                AppI18n.activeLanguage(),
                uh.getLastUpdateCheckResult(),
                uh.getBusy());
        var graphic = Bindings.createObjectBinding(
                () -> {
                    if (uh.getBusy().getValue() || uh.getLastUpdateCheckResult().getValue() != null) {
                        return "mdi2d-download";
                    }

                    return "mdi2r-refresh";
                },
                uh.getBusy(),
                uh.getLastUpdateCheckResult());
        return new TileButtonComp(name, description, graphic, actionEvent -> {
                    ThreadHelper.runFailableAsync(() -> {
                        AppDistributionType.get().getUpdateHandler().refreshUpdateCheck();
                        if (uh.getLastUpdateCheckResult().getValue() != null) {
                            UpdateAvailableDialog.showIfNeeded(false);
                        }
                    });
                    actionEvent.consume();
                })
                .styleClass("update-button")
                .disable(uh.getBusy())
                .createRegion();
    }
}
