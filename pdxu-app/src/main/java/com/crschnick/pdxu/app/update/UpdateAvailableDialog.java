package com.crschnick.pdxu.app.update;

import com.crschnick.pdxu.app.comp.Comp;
import com.crschnick.pdxu.app.comp.base.MarkdownComp;
import com.crschnick.pdxu.app.comp.base.ModalOverlay;
import com.crschnick.pdxu.app.issue.TrackEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class UpdateAvailableDialog {

    private static final Property<ModalOverlay> last = new SimpleObjectProperty<>();

    public static void showIfNeeded(boolean wait) {
        var l = last.getValue();
        if (l != null && l.isShowing()) {
            return;
        }

        UpdateHandler uh = AppDistributionType.get().getUpdateHandler();
        if (uh.getLastUpdateCheckResult().getValue() == null) {
            return;
        }

        // Check whether we still have the latest version prepared
        uh.refreshUpdateCheckSilent();
        if (uh.getLastUpdateCheckResult().getValue() == null) {
            return;
        }

        TrackEvent.withInfo("Showing update alert ...")
                .tag("version", uh.getLastUpdateCheckResult().getValue().getVersion())
                .handle();
        var u = uh.getLastUpdateCheckResult().getValue();

        var comp = Comp.of(() -> {
            var markdown = new MarkdownComp(u.getBody() != null ? u.getBody() : "", s -> s, false).createRegion();
            return markdown;
        });
        var modal = ModalOverlay.of("updateReadyAlertTitle", comp.prefWidth(600), null);
        for (var action : uh.createActions()) {
            modal.addButton(action);
        }

        last.setValue(modal);
        if (wait) {
            modal.showAndWait();
        } else {
            modal.show();
        }
    }
}
